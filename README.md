# AutoDing
钉钉打卡，包括定时打卡和远程推送打卡,定时打卡功能是根据AutoDingDing项目修改而来，远程打卡功能由极光推送SDK实现。
建议用一台闲置的手机放在公司来打卡，另一台手机来推送打卡指令。
基本用法只需要一台闲置手机，定好时间后会自动打开钉钉，完成极速打卡；
进阶用法 需要一台打卡手机，一台自用手机，把打卡手机的注册ID复制到推送界面推送打卡指令可以远程打开钉钉，完成极速打卡。设置须知：
1. 请先确认好通知栏监听已开启，如不开启将无法监听打卡成功的通知。
2. 调起钉钉实现自动打卡，需要把打卡手机锁屏密码之类的取消掉
3. 部分手机需要授权允许后台打开窗口
4. 先在设置里面测试下能否正常打开钉钉，第一次有些手机会让授权
5. 将钉钉软件上下班都设置为“极速打卡”。
6. 设置好自己的收信邮箱，跳转到“钉钉”打卡成功后会发送一封打卡成功的邮件到你自己设置好的邮箱。

+ 配置须知：如果要自行编译，请修改源代码中SendMailUtil.java中相关邮箱配置为自己邮箱，申请邮箱授权码的方法请自行上网查询
```java
public class SendMailUtil {
    @NonNull
    public static MailInfo createMail(String toAddress, String emailMessage) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost("smtp.qq.com");//发送方邮箱服务器
        mailInfo.setMailServerPort("587");//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName("youremail@qq.com"); // 发送者邮箱地址
        mailInfo.setPassword("yourAuthCode");//邮箱授权码，不是密码
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setFromAddress("youremail@qq.com"); // 发送者邮箱
        mailInfo.setSubject("自动打卡通知"); // 邮件主题
        if (emailMessage.equals("")) {
            mailInfo.setContent("未监听到打卡成功的通知，请手动登录检查" + TimeOrDateUtil.timestampToDate(System.currentTimeMillis())); // 邮件文本
        } else {
            mailInfo.setContent(emailMessage); // 邮件文本
        }
        return mailInfo;
    }
}

```