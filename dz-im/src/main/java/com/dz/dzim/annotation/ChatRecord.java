package com.dz.dzim.annotation;

import java.lang.annotation.*;

/**
 * 聊天记录注解
 * <p>
 * 加上这个注解的特定方法，会将聊天记录信息记录到文件中。
 * 特定方法是指方法必需以MessageVO类或该类的子类作为参数，
 * 如果有多个MessageVO类或该类的子类作为参数，则默认取第一个
 *
 * @author baohan
 * @date 2021/1/26
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)  // 运行时可见
@Target(ElementType.METHOD)   // 方法注解
public @interface ChatRecord {
}
