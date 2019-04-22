package org.aron.server.servlet.parser;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.commons.io.FileUtils;
import org.aron.commons.utils.Utils;
import org.aron.server.servlet.http.Multipart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MultipartParser {

    private String PATH = FileUtils.getResourcePath() + File.separator;
    private static final String SEMICOLON_BLANK = "; ";

    private String boundary;

    private ByteArrayOutputStream bos;

    private int mode;

    private Multipart multipart;

    /**
     * sha1 计算工具类
     */
    private MessageDigest messageDigest;

    /**
     * boundary -> Multipart
     */
    @Getter
    private Map<String, Multipart> multipartMap;

    public MultipartParser(String boundary, String path) {
        if (FileUtils.isAbsolutePath(path)) {
            this.PATH = path;
        } else {
            if (StringUtils.isNotBlank(path)) {
                this.PATH += path;
            } else {
                this.PATH += "tmp";
            }
        }
        this.boundary = boundary;
        this.bos = new ByteArrayOutputStream();
        this.multipartMap = new HashMap<>(0);
        if (!new File(PATH).exists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(PATH).mkdirs();
        }
        this.messageDigest = DigestUtils.getSha1Digest();
    }

    public void parseMultipart(byte[] bytes) throws IOException {
        parseBytes(bytes);
    }

    private void parseBoundary(int len) throws IOException {
//        log.debug("----------parse boundary----------");
        if (bos.size() > 0) {
            writeByteArrayToFile(multipart.getFile(), bos.toByteArray(), bos.size() - len);
            multipart.setSize(multipart.getFile().length());
            multipart.setSha1(Hex.encodeHexString(messageDigest.digest()));
            bos.reset();
            messageDigest.reset();
            multipart.setFile(FileUtils.rename(multipart.getFile(), multipart.getSha1()));
            multipart.setFilename(multipart.getFile().getName());
        }
//        log.debug("----------parse boundary finish----------");
    }

    private void parseBytes(byte[] bytes) throws IOException {
        int startIndex = 0, endIndex;
        for (int i = 0, len = bytes.length; i < len; i++) {
            if (bytes[i] == 10) {
                endIndex = i - 1;
                String line = new String(bytes, startIndex, endIndex - startIndex);
                startIndex = i;
                if (StringUtils.trim(line).equals("--" + boundary)) {
                    parseBoundary(("--" + boundary).getBytes().length + 3);
                    mode = 0;
                    mode ++;
                    continue;
                }
                if (mode == 1 && line.contains("Content-Disposition")) {
                    parseDisposition(line);
                    continue;
                }
                if (mode == 2 && line.contains("Content-Type")) {
                    String contentType = StringUtils.substringAfter(line, ":");
                    multipart.setContentType(StringUtils.trim(contentType));
                    mode ++;
                    continue;
                }
                if (mode == 3 && StringUtils.isBlank(line)) {
                    mode ++;
                    continue;
                }
                if (StringUtils.trim(line).equals("--" + boundary + "--")) {
                    parseBoundary(("--" + boundary + "--").getBytes().length + 3);
                    continue;
                }
            }
            if (mode == 4) {
                // 防止堆溢出
                if (FileUtils.heapSpaceOverflow(bos.size())) {
                    log.debug("----------即将超出jvm内存 优先写入文件----------");
                    writeByteArrayToFile(multipart.getFile(), bos.toByteArray(), bos.size());
                    bos.reset();
                    log.debug("----------优先写入文件完毕----------");
                }
                bos.write(bytes[i]);
            }
        }
    }

    /**
     * 将字节数组写入文件
     * 将字节数组更新到sha1解析器
     * @param file 目标文件
     * @param bytes 字节数组
     * @param len 目标长度
     */
    private void writeByteArrayToFile(File file, byte[] bytes, int len) throws IOException {
        FileUtils.writeByteArrayToFile(file, bytes, 0, len, true, false);
        messageDigest.update(bytes, 0, len);
    }

    /**
     * 解析Content-Disposition并替换Multipart对象
     */
    private void parseDisposition(String line) {
//        log.debug("----------parse Content Disposition----------");
        String tmp = StringUtils.substringAfter(line, SEMICOLON_BLANK);
        String[] infoArray = tmp.split(SEMICOLON_BLANK);
        String name = null, filename = null;
        for (String info : infoArray) {
            if (StringUtils.startsWithIgnoreCase(info, "name")) {
                name = StringUtils.substringAfter(info.replaceAll("\"", ""), "=");
                continue;
            }
            if (StringUtils.startsWithIgnoreCase(info, "filename")) {
                filename = StringUtils.substringAfter(info.replaceAll("\"", ""), "=");
            }
        }
        if (filename == null) {
            filename = name;
            mode = 3;
        } else {
            mode ++;
        }
        replaceMultipart(name, filename);
//        log.debug("----------parse Content Disposition finish----------");
    }

    /**
     * 替换Multipart对象
     * 初始化uuid, name, filename, File
     * @param name 请求name
     * @param filename 上传的文件名
     */
    private void replaceMultipart(final String name, final String filename) {
        final String uuid = Utils.generateUUID();
        this.multipart = new Multipart(){{
            setUuid(uuid);
            setName(name);
            setFilename(filename);
            setFile(new File(PATH, filename));
        }};
        multipartMap.put(uuid, multipart);
    }
}
