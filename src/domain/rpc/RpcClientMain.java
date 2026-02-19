package domain.rpc;

public class RpcClientMain {
    public static void main(String[] args) {
        CalculatorService calcClient = new CalculatorClientStub("127.0.0.1", 5000);
        System.out.println("add(2,3)="+ calcClient.add(2,3));
        System.out.println("square(9)="+ calcClient.square(9));
    }
}