# All to Scala от Тинькофф

## Задание 2

Описание задачи, Java

У вас есть метод `readData` для получения порции непрерывных данных. Данные нужно сразу отослать всем потребителям при
помощи `sendData`.

Ваша задача написать метод `performOperation`, который будет производить такую рассылку с максимальной пропускной
способностью.

Технические детали

1. Аргументы для `sendData` нужно брать из значения, возвращаемого `readData`

2. Каждый адресат из списка `Event.recipients` должен получить данные `payload`

3. Во время отправки данные могут быть:

* `Result.ACCEPTED` - приняты потребителем, операция отправки данных адресату `dest` считается завершённой
* `Result.REJECTED` - отклонены, операцию отправки следует повторить после задержки `timeout()`

4. Метод `performOperation` должен обладать высокой пропускной способностью: события внутри `readData` **могут
   накапливаться**

Сниппеты кода

```java
public record Payload(String origin, byte[] data) {
}

public record Address(String datacenter, String nodeId) {
}

public record Event(List<Address> recipients, Payload payload) {
}

public enum Result {ACCEPTED, REJECTED}

public interface Client {
    //блокирующий метод для чтения данных
    Event readData();

    //блокирующий метод отправки данных
    Result sendData(Address dest, Payload payload);
}

public interface Handler {
    Duration timeout();

    void performOperation();
}
```
