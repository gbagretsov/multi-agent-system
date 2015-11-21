package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;


public class Agent_TrafficLight extends Agent {

    private Hashtable<String, LinkedList<String>> cars;
    private List<String> outcoming;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        int[][] map = (int[][]) args[0];
        int index = Integer.parseInt(args[1].toString());

        /* Создаём очереди */
        cars = new Hashtable<String, LinkedList<String>>();
        for (int i = 0; i < map.length; i++) {
            if (map[i][index] == 1) {
                cars.put("tl_".concat(String.valueOf(i)), new LinkedList<String>());
            }
        }

        /* Создаём список исходящих */
        outcoming = new ArrayList<String>();
        for (int i = 0; i < map.length; i++) {
            if (map[index][i] == 1) {
                outcoming.add("tl_".concat(String.valueOf(i)));
            }
        }

        /* Debug output */
        String incomingString = "", outcomingString = "";
        for (String s : cars.keySet()) {
            incomingString = incomingString.concat(s + " ");
        }
        for (String s : outcoming) {
            outcomingString = outcomingString.concat(s + " ");
        }
        System.out.println("Debug: " + getAID().getLocalName()
                + "; in: "  + incomingString
                + "; out: " + outcomingString);

        addBehaviour(new IncomingCarsHandlerBehaviour());
        addBehaviour(new NewCarsComingToTownHandlerBehaviour());
    }

    private class IncomingCarsHandlerBehaviour extends CyclicBehaviour {
        MessageTemplate incomingCarTemplate = MessageTemplate.MatchOntology("incoming-car");

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(incomingCarTemplate);
            if (msg != null) {
                String from = msg.getSender().getLocalName();
                String car = msg.getContent();
                ((Agent_TrafficLight) getAgent()).putCarLocalNameToQueue(from, car);
                /* Debug */
                System.out.println("Debug: my name is " + getAgent().getLocalName()
                                 + "; car name is " + car
                                 + "; come from " + from
                                 + "; queue length is " + ((Agent_TrafficLight) getAgent()).getQueueLength(from));
            }
            else {
                block();
            }
        }
    };

    private class NewCarsComingToTownHandlerBehaviour extends CyclicBehaviour {
        MessageTemplate newCarTemplate = MessageTemplate.MatchOntology("coming-to-town");

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(newCarTemplate);
            if (msg != null) {
                String car = msg.getSender().getLocalName();
                String to = msg.getContent();
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID(to, AID.ISLOCALNAME));
                message.setOntology("incoming-car");
                message.setContent(car);
                send(message);
            }
            else {
                block();
            }
        }
    };

    private int getQueueLength(String tlLocalName) {
        return cars.get(tlLocalName).size();
    }

    private String getCarLocalNameFromQueue(String tlLocalName) {
        return cars.get(tlLocalName).pollFirst();
    }

    private void putCarLocalNameToQueue(String tlLocalName, String carLocalName) {
        cars.get(tlLocalName).addLast(carLocalName);
    }

}