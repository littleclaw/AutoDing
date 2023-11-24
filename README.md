# AutoDing
钉钉打卡，包括定时打卡和远程推送打卡,定时打卡功能是根据AutoDingDing项目修改而来，不是此项目重点，此项目主要功能在于实现远程
打开钉钉，从而完成极速打卡，其主要是用极光推送SDK实现网络指令下发。
建议用一台闲置的手机放在公司来打卡，另一台手机来推送打卡指令。
需要一台打卡手机，一台自用手机，都安装上此APP后，把打卡手机的注册ID复制到推送界面推送打卡指令可以远程打开钉钉，完成极速打卡。使用须知：
1. 请先确认好要打卡的手机通知栏监听已开启，如不开启将无法监听打卡成功的通知。
2. 要打卡的手机保持连网，wifi或者数据都可，否则怎么收网络指令啊。
3. 要打卡的手机最好保持打开界面放置灭屏，因为如果应用被切到后台，过上一晚不活跃有可能会被安卓系统杀掉从而不能接收打卡指令。
4. 调起钉钉实现自动打卡，需要把打卡手机锁屏密码之类的取消掉，屏幕不用常亮，但锁屏手势、指纹、密码这些验证不能有，否则在熄屏状态下应用接收到消息也无法唤醒屏幕。
5. 部分手机需要授权允许后台打开窗口，先在设置里面测试下能否正常打开钉钉，第一次有些手机会让授权
6. 将钉钉软件上下班都设置为“极速打卡”。
7. 设置好自己的发信和收信邮箱，跳转到“钉钉”打卡成功后会发送一封打卡成功的邮件到你自己设置好的邮箱。
8. 不要忘了，此应用只能打开钉钉，如果你钉钉没登录、被其他设备踢下线、打开时机不在考勤时间区间里，极速打卡是不会生效的。

+ 请在邮件配置中尽量使用自己的qq邮箱，申请邮箱授权码的方法请上网查询，目前邮箱只支持qq邮箱，因为邮箱服务器和端口在代码里写死了，想用别的邮箱的可以找到这部分代码修改这部分设置
=============
提高稳定性建议，经过数个月在两部手机上的使用，说一些提高远程打卡稳定性的建议，我感觉非常实用
1. 在应用权限设置里设置成允许锁屏显示，能大幅降低打卡唤醒应用时被各种不能取消的锁屏带来的干扰
2. 打卡手机尽量不要放在抽屉或柜子里等完全黑暗的环境中，可以放在桌面上盖住，但把前置摄像头露出来对外面。因为目前安卓机大多有前置摄像头感光检测，前摄检测不到光线的情况下，系统很快会进入深度睡眠模式，就会造成推送指令无法及时接收，甚至应用在系统休眠一段时间后进程被杀死的情况

