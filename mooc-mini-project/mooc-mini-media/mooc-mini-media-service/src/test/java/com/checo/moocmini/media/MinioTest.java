package com.checo.moocmini.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinioTest {

    // Create a minioClient with the MinIO server playground, its access key and secret key
    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.101.65:9000")
            .credentials("minioadmin", "minioadmin")
            .build();

    @Test
    public void testUploadFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // Make 'testbucket' bucket if not exist.
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("testbucket").build());
        if (!found) {
            // Make a new bucket called 'testbucket'.
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("testbucket").build());
        } else {
            System.out.println("Bucket 'testbucket' already exists.");
        }

        // Get content type
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null)
            mimeType = extensionMatch.getMimeType();

        // Upload '/home/user/Photos/asiaphotos.zip' as object name 'test.mp4' to bucket
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("C:/IT/TestGarbage/test.mp4")
                .contentType(mimeType)
                .object("test.mp4")
                .build();
        minioClient.uploadObject(uploadObjectArgs);
        System.out.println("'C:/IT/TestGarbage/test.mp4' is successfully uploaded as " + "object 'test.mp4' to bucket 'testbucket'.");
    }

    @Test
    public void testGetFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket")
                .object("test.mp4")
                .build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream outputStream = new FileOutputStream("C:/IT/TestGarbage/testCopy.mp4");
        IOUtils.copy(inputStream, outputStream);

        // 校验文件 MD5
        String sourceMd5 = DigestUtils.md5Hex(Files.newInputStream(Paths.get("C:/IT/TestGarbage/test.mp4")));
        String getFileMd5 = DigestUtils.md5Hex(Files.newInputStream(Paths.get("C:/IT/TestGarbage/testCopy.mp4")));
        if (sourceMd5 == getFileMd5) {
            System.out.println("下载成功");
        } else {
            File file = new File("C:/IT/TestGarbage/testCopy.mp4");
            boolean delete = file.delete();
            System.out.println("下载失败");
        }
    }

    @Test
    public void testDeleteFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("C:/IT/TestGarbage/testCopy.mp4")
                .object("test.mp4")
                .build();
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("test.mp4").build();
        minioClient.removeObject(removeObjectArgs);
    }

    /**
     * 将分块文件上传到 MinIO
     */
    @Test
    public void testUploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (int i = 0; i < 6; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("C:/IT/TestGarbage/BigFileTest/Chunk/" + i)
                    .object("Chunk/" + i)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块 " + i + "成功");
        }
    }

    /**
     * 调用 MinIO 接口合并分块
     */
    @Test
    public void testMergeChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket("testbucket")
                    .object("Chunk/" + i)
                    .build();
            sources.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }

    /**
     * 批量清理分块文件
     */
    @Test
    public void testBatchDeleteChunkFile() {
        //合并分块完成将分块文件清除
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(6)
                .map(i -> new DeleteObject("Chunk/".concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("testbucket").objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
