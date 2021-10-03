package group34;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.uncertainty.UserModel;

import java.util.*;
import java.util.stream.Collectors;

/***function: predict opponent info
 * method 1:  storeTable(UserModel userModel) : produce userModel all info list;
 * method 2: jonnyBlack(Bid lastOffer): predict opponent info by Jonny black method;
 * method 3: jbCalOpponUti(Bid lastOffer) : predict opponent utility;
 * ****/
public class OpponentInfo implements Comparator<OpponentInfo>{
    private Value valueName;
    public int optionNum=0;//count
    public int optionRank=0;//rank
    public int optionsSum=0;//totalOfOptions
    public int opponBidNum=0;// countBidNumber
    public double optionValue=0.0;//calculatedValue
    public double optionWeight=0;//weightUnnormalized
    public int oppontBidNum=0;//the number of opponent's bid countBidNumber

    Map<Issue,List<OpponentInfo>> allInfoList = new HashMap<>();//IaMap 类
    HashMap<Issue,Double> weightAll = new HashMap<>();//weightList

    public OpponentInfo(Value valueName) {
        this.valueName = valueName;
    }
    public OpponentInfo() {

    }

    public Value getValueName() {
        return valueName;
    }

    public void setValueName(Value valueName) {
        this.valueName = valueName;
    }

    @Override
    public int compare(OpponentInfo o1,OpponentInfo o2) {
        return (o1.optionNum>o2.optionNum)?-1:1;
    }


    //1.store table
    //IaMap
    public void storeTable(UserModel userModel){
        this.allInfoList = userModel.getDomain().getIssues().stream().collect(Collectors.toMap(k ->k,e-> ((IssueDiscrete) e).getValues().stream().map(OpponentInfo::new).collect(Collectors.toList())));
    }

    //2.jonnyBlack
    public void jonnyBlack(Bid lastOffer){
        double calWeightVar=0;//calculate weight unnormalized Intermediate variables
        double weightSum=0;//totalWeight
        double maxWeight=0;//maxWeightUnnormalized
        double issueWeightNormalized=0;//issueWeight
        double utility=0;
        this.opponBidNum+=1; //countBidNumber

        System.out.print("current issue all info: "+this.allInfoList);

        for (Issue issue:lastOffer.getIssues()){
            for(OpponentInfo opponentInfo:this.allInfoList.get(issue)){
                IssueDiscrete issueDiscrete = (IssueDiscrete) issue;

                opponentInfo.optionNum+=1;

                opponentInfo.optionsSum=issueDiscrete.getNumberOfValues();
                opponentInfo.opponBidNum=this.opponBidNum;
            }
            Collections.sort(this.allInfoList.get(issue),this.allInfoList.get(issue).get(0));
            for(OpponentInfo opponentInfo:this.allInfoList.get(issue)) {

                opponentInfo.optionRank = this.allInfoList.get(issue).indexOf(opponentInfo) + 1;

                //compute()
                opponentInfo.optionValue =((opponentInfo.optionsSum-opponentInfo.optionRank+1)/opponentInfo.optionsSum);
                calWeightVar=opponentInfo.optionNum/opponentInfo.opponBidNum;
                opponentInfo.optionWeight =Math.pow(calWeightVar,2);

                weightSum=opponentInfo.optionWeight+weightSum;

                //calculate each issue weight
                if (opponentInfo.optionWeight>maxWeight){
                    maxWeight=opponentInfo.optionWeight;
                }


            }


            //calculate each issue weight
            issueWeightNormalized=maxWeight/weightSum;
            this.weightAll.put(issue,issueWeightNormalized);

            for (OpponentInfo opponentInfo:this.allInfoList.get(issue)){
                //calculate utility
                if(opponentInfo.valueName.toString().equals(lastOffer.getValue(issue.getNumber()).toString())){
                    utility += this.weightAll.get(issue)*opponentInfo.optionValue;
                    break;
                }
            }



        }

    }
    //predict opponent utility when I give bid
    //JBpredict

    //哪里用到了？？？
    public double jbCalOpponUti(Bid lastOffer){
        double oppentUtility=0;
        for(Issue issue:lastOffer.getIssues()){
            for (OpponentInfo opponentInfo:this.allInfoList.get(issue)){
                if(opponentInfo.valueName.toString().equals(lastOffer.getValue(issue.getNumber()).toString())){
                    oppentUtility+=this.weightAll.get(issue)*opponentInfo.optionValue;
                    break;
                }
            }
        }
        return oppentUtility;
    }


}
