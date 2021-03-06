package chat;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageList implements Serializable {

    public class Message implements Serializable {
        private String sender;
        private String message;
        private boolean isUnread;

        Message(String sender, String message, boolean isUnread) {
            this.sender = sender;
            this.message = message;

            this.isUnread = !isUnread;
        }

        public String getMessage() {
            if (isUnread) {
                return "" + sender + ": " + message;
            } else {
                return sender + ": " + message;
            }

        }

        public void setRead() {
            this.isUnread = false;
        }

        public boolean isUnread() {
            return isUnread;
        }

        public String getSender() {
            return sender;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "sender='" + sender + '\'' +
                    ", message='" + message + '\'' +
                    ", isUnread=" + isUnread +
                    '}';
        }
    }

    private List<Message> messagesList = new ArrayList<>();

    public void addMessage(String name, String message, boolean isUnread) {
        messagesList.add(new Message(name, message, isUnread));
    }

    public List<Message> getMessagesList() {
        return messagesList;
    }

    public boolean containsUnread() {
        for (Message message : messagesList) {
            if (message.isUnread()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "MessageList{" +
                "messagesList=" + messagesList +
                '}';
    }
}

