# gen-thrift-maven-plugin
将thrift文件生成目标xx语言文件

运行环境：

1.安装thrift，mac下使用brew install thrift

2.通用安装方法，去官网下载并解压https://thrift.apache.org/download。
         进入解压后的目录执行configure，make，make install

使用方法：

1.使用maven打包后，在别的项目引用即可

```$xslt
<plugin>
    <groupId>com.acme.plugins</groupId>
    <artifactId>gen-thrift</artifactId>
    <version>xxx</version>
</plugin>
```

2.之后和使用其他插件一样使用就行

插件扩展：

目前只有最简单的自动生成目标语言文件功能，加了配置文件的功能。

版本更新：
V1.0.1
*去掉文件配置的方式，改用maven常用的configuration标签设置参数
*生成java文件，直接指定生成目录
*支持修改输出目录

