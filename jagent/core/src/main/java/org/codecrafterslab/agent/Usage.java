package org.codecrafterslab.agent;

import org.codecrafterslab.agent.logger.Logger;
import org.codecrafterslab.agent.logger.impl.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用法提示工具类，打印使用说明
 *
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2017/10/06 07:07
 */
public class Usage {

    /** 日志记录器 */
    private final static Logger log = LoggerFactory.getLogger(Usage.class);

    /**
     * 主入口，从 resources/usage.txt 读取并打印使用说明
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        InputStream io = Usage.class.getResourceAsStream("/usage.txt");
        String line;

        assert io != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(io));
        try {
            while ((line = br.readLine()) != null) {
                log.info(line);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
