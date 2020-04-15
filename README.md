# WebviewWithUploadImg

## 背景：
&emsp;&emsp;之前做项目，主要是一个webview包壳，但因为需要很多和原生交互的东西，所以代码逐步累加，就遇到的一些小问题，做一次整理，主要做个小demo。
## 主要一些东西：
    1. tbs框架的webview
        * tbs在application初始化
        * webview动态生成，注意销毁
    2. webview加载H5调用拍照，上传图片功能，不会写H5页面，在别人博客上复制了一个！
    3. 使用pdf.js读取pdf文件
        * pdf 读取pdf文件还没写```和window.open放一起吧！
    4. webview遇到H5的window.open问题
        * 新建一个窗口 WindowOpenActivity,这个页面没有加沉浸式处理````懒得写而已！就一个方法调用的事情````
        * url根据请求头去判断类型content-type，根据类型判断是否加载pdf.js还是直接加载url，当然播放视频，音频等等的都可以这么处理吧！
    5. 动态权限申请
        * 有些厂商的小部分手机默认授权了一些权限，所以可能不会走权限回调
    6. 关于webview包壳的沉浸式效果的实现`````如果顶部是轮播图不太友好！
        * 凑合来吧！这个暂时没想到好办法！
