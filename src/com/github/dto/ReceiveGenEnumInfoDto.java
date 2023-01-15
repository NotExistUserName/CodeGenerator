package com.github.dto;

/**
 * @Author CoffeeEngineer
 * @Description 获取枚举生成信息实体
 * @Date 2022/12/16 22:18
 */
public class ReceiveGenEnumInfoDto {

    /**
     * 枚举信息串
     */
    private String enumInfoString;

    /**
     * 枚举编码
     */
    private String enumCode;

    public String getEnumInfoString() {
        return enumInfoString;
    }

    public ReceiveGenEnumInfoDto setEnumInfoString(String enumInfoString) {
        this.enumInfoString = enumInfoString;
        return this;
    }

    public String getEnumCode() {
        return enumCode;
    }

    public ReceiveGenEnumInfoDto setEnumCode(String enumCode) {
        this.enumCode = enumCode;
        return this;
    }

    @Override
    public String toString() {
        return "ReceiveGenEnumInfoDto{" +
                "enumInfoString='" + enumInfoString + '\'' +
                ", enumCode='" + enumCode + '\'' +
                '}';
    }
}
