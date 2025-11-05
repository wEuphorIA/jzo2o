//package com.jzo2o.mvc.utils;
//
//import com.jzo2o.common.model.CurrentUser;
//
//import java.util.Objects;
//
///**
// * 用户信息上下文，主要存储用户id
// *
// * @author itcast
// */
//public class UserContext {
//    private static ThreadLocal<CurrentUser> THREAD_LOCAL_USER = new ThreadLocal<CurrentUser>();
//
//    /**
//     * 获取当前用户id
//     *
//     * @return 用户id
//     */
//    public static Long currentUserId() {
//        return THREAD_LOCAL_USER.get().getId();
//    }
//    public static String currentUserIdString() {
//        CurrentUser currentUserInfo = THREAD_LOCAL_USER.get();
//        if (currentUserInfo == null){
//            return null;
//        }
//        return currentUserInfo.getIdString();
//    }
//    public static  <T extends CurrentUser> T  currentUser() {
//        CurrentUser currentUserInfo = THREAD_LOCAL_USER.get();
//        if (currentUserInfo == null){
//            return null;
//        }
//        return getCurrentUser();
//    }
//
//    public static  <T extends CurrentUser> T getCurrentUser(){
//        T t = (T) Objects.requireNonNull(THREAD_LOCAL_USER.get());
//        return t;
//    }
//
////    public static CurrentUserInfo getCurrentUser() {
////        return currentUser();
////    }
//
//    /**
//     * 设置当前用户id
//     *
//     * @param currentUserInfo 当前用户信息
//     */
//    public static void set(CurrentUser currentUserInfo) {
//        THREAD_LOCAL_USER.set(currentUserInfo);
//    }
//
//    /**
//     * 清理当前线程中的用户信息
//     */
//    public static void clear(){
//        THREAD_LOCAL_USER.remove();
//    }
//
//
//}
