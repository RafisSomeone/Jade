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

package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SlaveAgent extends Agent {
    private String jobName;
    private AID[] masterAgents;
    private AID receiver;

    protected void setup() {
        System.out.println("Hallo! Slave "+getAID().getName()+" is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            jobName = (String) args[0];
            receiver = new AID((String)args[1], AID.ISGUID);
            System.out.println("Target job is "+jobName);
            addBehaviour(new OneShotBehaviour(this) {
                @Override
                public void action() {
                    myAgent.addBehaviour(new RequestPerformer());
                }
            } );
        }
    }

    protected void takeDown() {
        System.out.println("Slave-agent "+getAID().getName()+" terminating.");
    }

    public class RequestPerformer extends Behaviour {

        public void action() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.addReceiver(receiver);
            cfp.setContent("done");
            cfp.setConversationId("book-trade");
            cfp.setReplyWith("cfp"+System.currentTimeMillis());
            myAgent.send(cfp);
        }

        public boolean done() {
            System.out.println("Done");
           return true;
        }
    }
}
