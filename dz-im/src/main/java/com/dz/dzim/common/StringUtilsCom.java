package com.dz.dzim.common;/**
 * @description: some desc
 * @author: lenovo
 * @email: xxx@xx.com
 * @date: 2021/1/25 11:33
 */

/**
 * @author xxxyyy
 * @className StringUtilsCom
 * @description TODO
 * @date 2021/1/25 11:33
 */
public class StringUtilsCom {

    public static boolean isStringLenth(String[] s) {
        if (null == s || s.length == 0 || "".equals(s)) {
            return false;
        }
        if (s.length > 0) {
            if (null == s[0] || "".equals(s[0])) {
                return false;
            }
        }
        return true;
    }



}
