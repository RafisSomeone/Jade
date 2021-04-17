package agents.meeting;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingManager extends Agent {
    private Map<AID, anAgent.agentType> agents = new ConcurrentHashMap<>();
    public AID MeetingManagerAID = this.getAID();
    private Gui myGui;
    private boolean meetingStarted = false;

    public void startMeeting() {
        ACLMessage startMessage = new ACLMessage(ACLMessage.CFP);
        for (AID aid : agents.keySet()) {
            startMessage.addReceiver(aid);
        }
        startMessage.setContent("start");
        startMessage.setConversationId("start-meeting");
        startMessage.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
        this.send(startMessage);
        meetingStarted = true;
    }

    protected void setup() {
        myGui = new Gui(this);
        myGui.showGui();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("meeting");
        sd.setName("JADE-meeting");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new Register());
        addBehaviour(new Unregister());
        addBehaviour(new ConnectAgents());
    }

    private class Register extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                agents.put(msg.getSender(), anAgent.agentType.valueOf(msg.getContent()));
                System.out.println("Register " + msg.getSender().getName());
            } else {
                block();
            }
        }
    }

    private class Unregister extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                agents.remove(msg.getSender());
                System.out.println("Unregister " + msg.getSender().getName());
            } else {
                block();
            }
        }
    }

    private class ConnectAgents extends CyclicBehaviour {
        public void action() {
            if (meetingStarted) {
                Optional<AID> buyer = agents.entrySet().stream().filter(x -> x.getValue() == anAgent.agentType.BUYER).findFirst().map(Map.Entry::getKey);
                Optional<AID> seller = agents.entrySet().stream().filter(x -> x.getValue() == anAgent.agentType.SELLER).findFirst().map(Map.Entry::getKey);
                if (buyer.isPresent() && seller.isPresent()) {
                    AID buyerAID = buyer.get();
                    AID sellerAID = seller.get();
                    ACLMessage meetWithMsg = new ACLMessage(ACLMessage.INFORM);
                    meetWithMsg.addReceiver(buyerAID);
                    meetWithMsg.setContent(sellerAID.getName());
                    meetWithMsg.setConversationId("meet-with");
                    meetWithMsg.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(meetWithMsg);
                    agents.put(buyerAID, anAgent.agentType.NONE);
                    agents.put(sellerAID, anAgent.agentType.NONE);
                }
            }
        }
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("MeetingManager " + getAID().getName() + " terminating.");
    }

    public boolean isMeetingStarted() {
        return meetingStarted;
    }

    public void setMeetingStarted(boolean flag) {
        meetingStarted = flag;
    }
}
