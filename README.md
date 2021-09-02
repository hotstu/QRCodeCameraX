[![author](https://img.shields.io/badge/author-hglf-blue.svg)](https://github.com/hotstu) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![androidx](https://img.shields.io/badge/target-androidx-blue.svg)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/hotstu/QRCodeCameraX)

# QRCodeCameraX
QRcode decoder based on CameraX &amp; zxing-core &amp; ML kit, in less than 50 lines


基于CameraX api，代码极度精简，没有垃圾代码，直接操作yuvimage buffer，识别速度快

内置两种识别方式可以随意切换

QRcodeAnalyzer: 基于zxing-core的二维码识别

MLQRcodeAnalyzer: 基于firebase 机器视觉的二维码识别，(需要设备安装google paly Service，否则无法使用)

article：https://www.jianshu.com/p/3ce81b55468d

后记：目前camerax还没有发布稳定版，很多api都在变化中,所以如果升级了依赖版本可能会报很多错，等google发布正式版本吧

#### 2020.4.2日更新：

CameraX1.0.0-beta02已经发布，不出所料大量的breaking changes，目前网上的资料包括官方的教程还是旧的，这里放出最新的适配：

beta02的版本：[QRCodeCameraX/beta02](https://github.com/hotstu/QRCodeCameraX/tree/beta02)

#### 2021.6.18日更新：
CameraX1.0.0已经发布，API 基本稳定
