package cube.ware.ui.chat.message.Listener;


import cube.service.message.model.FileMessage;

/**
 * 文件消息下载监听器
 *
 * @author PengZhenjin
 * @date 2016-11-17
 */
public interface FileMessageDownloadListener {
    void onDownloading(FileMessage fileMessage, long processed, long total);

    void onDownloadCompleted(FileMessage fileMessage);
}
