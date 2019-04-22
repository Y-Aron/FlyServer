package org.aron.server.servlet.http;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import static org.aron.commons.constant.CharConstant.COABLANK;
import static org.aron.commons.constant.CharConstant.CRLF;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 12:37
 **/
@Data
public class Cookie {
    private String key;

    private String value;

    private String domain;

    private String path = "/";

    private boolean httpOnly;

    // https的属性 暂不支持
    private boolean secure;

    private long maxAge;

    public Cookie(String key, String value) {
        this.key = key;
        this.value = value;
        this.maxAge = -1;
    }

    /**
     * Set－Cookie: NAME=VALUE; maxAge=MAX_AGE; Path=PATH; Domain=DOMAIN_NAME; SECURE; HttpOnly
     * 当domain!=null || domain!=""时设置
     * 当path!=null || path!=""时设置
     * 当httpOnly == true时设置
     * 当max-age > 0 时设置
     * 当secure == true时设置
     * @return 响应的cookie
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Set-Cookie: ").append(key).append("=").append(value);
        if (StringUtils.isNotBlank(domain)) {
            sb.append(COABLANK).append("domain=").append(domain);
        }
        if (StringUtils.isNotBlank(path)) {
            sb.append(COABLANK).append("path=").append(path);
        }
        if (httpOnly) {
            sb.append(COABLANK).append("HttpOnly");
        }
        if (maxAge > -1) {
            sb.append(COABLANK).append("max-age=").append(maxAge);
        }
        if (secure) {
            sb.append(COABLANK).append("secure");
        }
        sb.append(CRLF);
        return sb.toString();
    }
}
