package org.aron.server.servlet.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aron.commons.constant.CharsetConstant;
import org.aron.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 16:39
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Multipart {
    private String uuid;
    private String name;
    private String filename;
    private File file;
    private long size;
    private String contentType;
    private String sha1;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String getText() throws IOException {
        String text = new String(FileUtils.readFileToByteArray(this.file),
                CharsetConstant.UTF_8_CHARSET);
        file.delete();
        return text;
    }
}
