import jade.Boot;

public class Main {

    public static void main(String[] args){
        String[] container = {
                "-gui",
                "-local-host 127.0.0.1",
                "-container",
                "Agent1:agents.PingAgent"   // <- Your custom agents
        };
        Boot.main(container);
        String[] newContainer = {
                "-container"
        };
        Boot.parseCmdLineArgs(newContainer);
//        New container on port 1099
//        java -cp jade-4.5.0.jar:classes jade.Boot -container -host localhost -port 1099

    }
}
