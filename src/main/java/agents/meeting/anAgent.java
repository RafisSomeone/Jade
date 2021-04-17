/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A.

 GNU Lesser General Public License

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation,
 version 2.1 of the License.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package agents.meeting;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class anAgent extends Agent {

    public enum agentType {
        SELLER,
        BUYER,
        NONE
    }

    Random ran = new Random();
    private Meeting meeting = new Meeting();
    private int step = 0;
    private boolean isDone = false;

    private MessageTemplate mt;

    private agentType type = ran.nextInt(2) == 1 ? agentType.SELLER : agentType.BUYER;

    protected void setup() {

        System.out.println("Hallo! Agent " + this.type.name() + " " + getAID().getName() + " is ready.");
        meeting.getPlace(this);
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                myAgent.addBehaviour(new Ready());
            }
        });

    }
    protected void takeDown() {
        System.out.println(getAID().getName() + " " + type.name() + " terminating.");
    }

    public class Ready extends Behaviour {
        public void action() {
            switch (step) {
                case 0:
                    System.out.println(getAID().getName() + " " + type.name() + " is ready");
                    meeting.ready(myAgent);
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {

                        if (reply.getPerformative() == ACLMessage.INFORM && type == agentType.BUYER) {
                            //meeting started
                            AID receiver = new AID(reply.getContent(), AID.ISGUID);
                            ACLMessage buyMessage = new ACLMessage(ACLMessage.PROPOSE);
                            buyMessage.addReceiver(receiver);
                            buyMessage.setContent("buy");
                            buyMessage.setConversationId("trade");
                            buyMessage.setReplyWith("cfp" + System.currentTimeMillis());
                            myAgent.send(buyMessage);
                            System.out.println(getAID().getName() + " " + type.name() + " bought the goods");
                            meeting.leave(myAgent);
                            isDone = true;
                        }
                        if (reply.getPerformative() == ACLMessage.PROPOSE && type == agentType.SELLER) {
                            System.out.println(getAID().getName() + " " + type.name() + " sold the goods");
                            meeting.leave(myAgent);
                            isDone = true;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (isDone) {
                System.out.println("Delete " + getAID().getName() + " " + type.name());
                myAgent.doDelete();
            }
            return isDone;
        }
    }

    public class Meeting {

        public AID managerAID;

        public void getPlace(Agent myAgent) {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("meeting");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                managerAID = result[0].getName();
            } catch (Exception ignored) {
            }
        }
        public void ready(Agent myAgent) {
            ACLMessage readyMessage = new ACLMessage(ACLMessage.CFP);
            readyMessage.addReceiver(managerAID);
            readyMessage.setContent(type.name());
            readyMessage.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(readyMessage);
        }
        public void leave(Agent myAgent) {
            ACLMessage readyMessage = new ACLMessage(ACLMessage.CANCEL);
            readyMessage.addReceiver(managerAID);
            readyMessage.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(readyMessage);
        }
    }
}
