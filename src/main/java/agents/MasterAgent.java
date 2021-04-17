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

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.*;

public class MasterAgent extends Agent {
    private LinkedHashSet<String> catalogue;
    private Gui myGui;

    protected void setup() {
        catalogue = new LinkedHashSet<>();

        myGui = new Gui(this);
        myGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("job");
        sd.setName("JADE-job");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new GetResult());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        myGui.dispose();
        System.out.println("Master-agent "+getAID().getName()+" terminating.");
    }

    public void updateCatalogue(final String job) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogue.add(job);
                try {
                    String slaveNick = String.valueOf(new Random().nextInt());
                    String[] jobInfo = new String[]{ job, getName() };
                    AgentController agent =  getContainerController()
                            .createNewAgent(slaveNick, "agents.SlaveAgent", jobInfo );
                    agent.start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
                System.out.println(job+" inserted into catalogue");
            }
        } );
    }

    private class GetResult extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String title = msg.getContent();
                myGui.showMessage(title);
            }
            else {
                block();
            }
        }
    }
}
