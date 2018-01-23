package message;

public class MessageContent extends Message {

    private String content;

    public MessageContent(String content){
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
