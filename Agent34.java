package group34;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;

import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;


/**
 * ExampleAgent returns the bid that maximizes its own utility for half of the negotiation session.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 * author: Yadi Chen(Lara)
 */
public class Agent34 extends AbstractNegotiationParty {

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;


    private AbstractUtilitySpace predictAbstractSpace;  //？？？ 不一定对
    private AdditiveUtilitySpace predictAdditiveSpace;
    //private UserModel userModel; //咋没用上呢？？？
    private  static double CONCESSION = 0.78;

    private static double MINIMUM_TARGET = 0.8;
    OpponentInfo opponentInfo = new OpponentInfo();

    Bid maxBid;
    Bid minBid;


    // step 1: entrance- predict my model
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        opponentInfo.storeTable(userModel);
        UserModelInfo userModelInfo = new UserModelInfo(userModel);


        this.predictAbstractSpace=userModelInfo.predictUtility();
        this.predictAdditiveSpace=(AdditiveUtilitySpace) predictAbstractSpace;
        BidRanking bidRanking = this.userModel.getBidRanking();
        maxBid = bidRanking.getMaximalBid(); //maxBidForMe
        minBid = bidRanking.getMinimalBid();
        /***************************/

        // utilitySpace: preference profile
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        //get current domain. next object, and then get all issues
        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();

        for (Issue issue : issues) {
            // get the index of issue
            int issueNumber = issue.getNumber();
            //get weight
            System.out.println(">> " + issue.getName() + " weight: " + additiveUtilitySpace.getWeight(issueNumber));

            // Assuming that issues are discrete only
            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            //get all issue->items evaluation
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);

            //print all value and evaluation
            for (ValueDiscrete valueDiscrete : issueDiscrete.getValues()) {
                System.out.println(valueDiscrete.getValue());
                System.out.println("Evaluation(getValue): " + evaluatorDiscrete.getValue(valueDiscrete));
                try {
                    System.out.println("Evaluation(getEvaluation): " + evaluatorDiscrete.getEvaluation(valueDiscrete));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    //3 actions: accept, generating a offer, ending

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        // According to Stacked Alternating Offers Protocol list includes
        // Accept, Offer and EndNegotiation actions only.
        double time = getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
        // The time is normalized, so agents need not be
        // concerned with the actual internal clock.
        if (time<0.1){
            return new Offer(this.getPartyId(),this.getMaxUtilityBid());
        }else if (time<0.3){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>CONCESSION ){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(this.getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.5){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>CONCESSION-0.02 ){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        } else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.6){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>CONCESSION-0.04 ){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.7){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>(CONCESSION-0.06)){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.8){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>(CONCESSION-0.08)){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.85){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>(CONCESSION-0.12)){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.9){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>(CONCESSION-0.12) ||
                    this.utilitySpace.getUtility(lastReceivedOffer)>this.opponentInfo.jbCalOpponUti(this.lastReceivedOffer)){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else if (this.lastReceivedOffer!=null&&this.myLastOffer!=null&&time<0.95){
            if (this.utilitySpace.getUtility(lastReceivedOffer)>(CONCESSION-0.13) ||
                    this.utilitySpace.getUtility(lastReceivedOffer)>this.opponentInfo.jbCalOpponUti(this.lastReceivedOffer)){
                return new Accept(getPartyId(),this.lastReceivedOffer);
            }else {
                return new Offer(getPartyId(),generateRandomBidAboveTarget());
            }
        }else {
            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(myLastOffer)) {

                return new Accept(this.getPartyId(),lastReceivedOffer);
            } else {
                // Offering a random bid
                myLastOffer = generateRandomBid();
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }



    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();
            this.opponentInfo.storeTable(this.userModel);
            this.opponentInfo.jonnyBlack(lastReceivedOffer);
        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        String description = "Group34";
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //get a minimum utility
    private Bid getMinUtilityBid(){
        try{
            return this.utilitySpace.getMinUtilityBid();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Bid generateRandomBidAboveTarget()
    {
        Bid randomBid;
        double util;
        double bidTime = getTimeLine().getTime();
        //double opponentUtility;
        Bid idealBid;
        BidRanking bidRanking = this.userModel.getBidRanking();
        List<Double> utilityList = new ArrayList<>();
        List<Double> opponentUtilityList = new ArrayList<>();
        List<Bid> randomBidList = new ArrayList<>();

        int i = 0;
        // try 100 times to find a bid under the target utility
        do
        {
            randomBid = generateRandomBid();
            util = utilitySpace.getUtility(randomBid);
        }
        while (util < MINIMUM_TARGET && i++ < 100);

        if(bidTime>=0.5){
            for (int j=0;j<this.getDomain().getNumberOfPossibleBids()*3;j++){

                if (util> CONCESSION-0.05&&util< CONCESSION+0.05){
                    randomBid = generateRandomBid();
                    utilityList.add(util);
                    opponentUtilityList.add(predictAbstractSpace.getUtility(randomBid));
                    randomBidList.add(randomBid);
                }

            }
            if (utilityList.size()!=0){
                idealBid = randomBidList.get(opponentUtilityList.indexOf(Collections.max(opponentUtilityList)));

            }else {
                for (int k=0;k<this.getDomain().getNumberOfPossibleBids()*2;k++){
                    randomBid = generateRandomBid();
                    randomBidList.add(randomBid);
                    utilityList.add(predictAbstractSpace.getUtility(randomBid));
                }
                idealBid = randomBidList.get(utilityList.indexOf(Collections.max(utilityList)));

            }
        }else if (bidTime>=0.1&&bidTime<0.5){
            for (int m=0;m<this.getDomain().getNumberOfPossibleBids()*2;m++){
                randomBid = generateRandomBid();
                util = predictAdditiveSpace.getUtility(randomBid);

                if (util< CONCESSION+0.05&&util> CONCESSION-0.1){
                    utilityList.add(util);
                    opponentInfo.storeTable(this.userModel);
                    opponentUtilityList.add(opponentInfo.jbCalOpponUti(lastReceivedOffer));
                    randomBidList.add(randomBid);
                }


            }
            if (utilityList.size()==0){
                for (int n=0;n<this.getDomain().getNumberOfPossibleBids()*2;n++){
                    randomBid = generateRandomBid();
                    randomBidList.add(randomBid);
                    utilityList.add(predictAbstractSpace.getUtility(randomBid));
                }
                idealBid = randomBidList.get(utilityList.indexOf(Collections.max(utilityList)));
                System.out.println("1: idealbid is:"+idealBid);
            }
            idealBid = randomBidList.get(utilityList.indexOf(Collections.max(utilityList)));
            System.out.println("2: idealbid is:"+idealBid);
        }else {
            idealBid = bidRanking.getMaximalBid();
            System.out.println("3: idealbid is:"+idealBid);
        }

        return idealBid;

    }



}
