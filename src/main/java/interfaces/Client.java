package interfaces;

import java.util.List;

public interface Client {
    //блокирующий метод для чтения данных
    Event readData();

    //блокирующий метод отправки данных
    Result sendData(Address dest, Payload payload);

    public record Payload(String origin, byte[] data) {
    }

    public record Address(String datacenter, String nodeId) {
    }

    public record Event(List<Address> recipients, Payload payload) {
    }

    public enum Result {ACCEPTED, REJECTED}
}
