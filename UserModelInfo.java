package group34;

import genius.core.Bid;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.UserModel;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;

import java.util.*;
import java.util.stream.Collectors;

public class UserModelInfo {
    //GeneticAlgorithm

    public UserModel userModel;
    public int popSum = 400; //popSize
    public List<AbstractUtilitySpace> population=new ArrayList<AbstractUtilitySpace>();

    public UserModelInfo(UserModel userModel){
        this.userModel=userModel;
    }



    //predict utility space
    //geneticAlgorithm
    public AbstractUtilitySpace predictUtility(){
        Random r=new Random();
        List<Double> bestPopulationList=new ArrayList<>();//123 lastFitnessList
        double bestPopulation;//bestFitness

        int i=0;
        while (i<2000){
            //123 getRandomChromosome()
            //generate utility space
            AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(userModel.getDomain());
            List<Issue> issueList = additiveUtilitySpaceFactory.getDomain().getIssues(); //issues
            for (Issue issue:issueList){
                additiveUtilitySpaceFactory.setWeight(issue,Math.random());
                IssueDiscrete valueList = (IssueDiscrete) issue; //values
                for(Value value:valueList.getValues()){
                    additiveUtilitySpaceFactory.setUtility(issue,(ValueDiscrete) value,Math.random());
                }
            }
            additiveUtilitySpaceFactory.normalizeWeights();
            population.add(additiveUtilitySpaceFactory.getUtilitySpace());
            i++;
        }

        int j=0;
        while (j<160){

            List<Double> qualityList=population.stream().map(this::qualityMetric).collect(Collectors.toList());

            //select
            population = roulette(population,qualityList,popSum);

            //crossover,crossover的时候考虑变异
            for(int m=0;m<(popSum/10);m++){
                AdditiveUtilitySpace crossoverDad =(AdditiveUtilitySpace)population.get(r.nextInt(popSum));
                AdditiveUtilitySpace crossoverMom = (AdditiveUtilitySpace)population.get(r.nextInt(popSum));
                //crossover
                AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(userModel.getDomain());
                List<IssueDiscrete> issueDiscreteList= additiveUtilitySpaceFactory.getIssues(); //issues

                for (IssueDiscrete issueDiscrete:issueDiscreteList){
                    double weightF = crossoverDad.getWeight(issueDiscrete);
                    double weightM = crossoverMom.getWeight(issueDiscrete);
                    double weightSum =(weightF+weightM)*0.5; //wUnion
                    double weightS=0;
                    if(Math.random()<0.50){
                        weightS = weightSum-0.35*(Math.abs(weightF-weightM));

                    }else {
                        weightS = weightSum+0.35*(Math.abs(weightF-weightM));
                    }
                    if (weightS<0.010){
                        weightS=0.010;
                    }
                    additiveUtilitySpaceFactory.setWeight(issueDiscrete,weightS);

                    //3. 变异情况
                    if (r.nextDouble()<0.04){
                        additiveUtilitySpaceFactory.setWeight(issueDiscrete,r.nextDouble());
                    }

                    //4.
                    for (ValueDiscrete valueDiscrete:issueDiscrete.getValues()){
                        weightF =((EvaluatorDiscrete)crossoverDad.getEvaluator(issueDiscrete)).getDoubleValue(valueDiscrete);
                        weightM =((EvaluatorDiscrete)crossoverMom.getEvaluator(issueDiscrete)).getDoubleValue(valueDiscrete);
                        if (Math.random()<0.5){
                            weightS = (weightF+weightM)/2-0.35*(Math.abs(weightF-weightM));
                        }else {
                            weightS = (weightF+weightM)/2+0.35*(Math.abs(weightF-weightM));
                        }
                        if (weightS<0.010){
                            weightS=0.010;
                        }
                        additiveUtilitySpaceFactory.setUtility(issueDiscrete,valueDiscrete,weightS);
                        if (r.nextDouble()<0.35){
                            additiveUtilitySpaceFactory.setUtility(issueDiscrete,valueDiscrete,r.nextDouble());
                        }
                    }


                }
                additiveUtilitySpaceFactory.normalizeWeights();
                AbstractUtilitySpace crossoverSon = additiveUtilitySpaceFactory.getUtilitySpace();
                population.add(crossoverSon);
            }

            j++;
        }
        for (AbstractUtilitySpace abstractUtilitySpace:population){
            bestPopulationList.add(qualityMetric(abstractUtilitySpace));
        }
        bestPopulation=Collections.max(bestPopulationList);
        qualityMetric(population.get(bestPopulationList.indexOf(bestPopulation)));


        return population.get(bestPopulationList.indexOf(bestPopulation)); //？？？需要改不？
    }

    //123 getFitness
    public double qualityMetric(AbstractUtilitySpace abstractUtilitySpace){
        BidRanking bidRanking = userModel.getBidRanking(); //123 bidRanking
        List<Bid> bidRankList = new ArrayList<>(); //123 bidRankingStore
        List<Bid> bidsList = new ArrayList<>();// 123 bidList
        List<Double> utilityList = new ArrayList<>();
        TreeMap<Integer,Double> utilityIndexList = new TreeMap<>(); //123 utilityRank
        double quality;
        for (Bid bid:bidRanking){
            bidRankList.add(bid);
        }

        int bidRankingSize = bidRanking.getSize()/400;
        int i=0;
        if (bidRankingSize<4){
            i = bidRankingSize;
        }else {
            i =3;
        }
        System.out.println("bidRankingSize is:"+bidRankingSize);

        switch (i){
            case 0:
                for (Bid bid:bidRanking){bidsList.add(bid);}
                break;
            case 1:
                for (Bid bid:bidRanking){bidsList.add(bid);}
                break;
            case 2:
                for (int j=0;j<bidRankingSize;j+=2){
                    bidsList.add(bidRankList.get(j));
                }
                break;
            case 3:
                for (int j=0;j<bidRankingSize;j+=i){
                    bidsList.add(bidRankList.get(j));
                }
                break;
        }

        for (Bid bid:bidsList){
            utilityList.add(abstractUtilitySpace.getUtility(bid));
        }

        int k=0;
        while (k<utilityList.size()){
            utilityIndexList.put(k,utilityList.get(k));
            k++;
        }


        //???
//        Comparable<Map.Entry<Integer,Double>> sortUtilityIndex= Comparator.comparingDouble(Map.Entry::getValue);//valueComparator
        List<Map.Entry<Integer,Double>> sortList = new ArrayList<>(utilityIndexList.entrySet());
//        /Collections.sort(sortList,sortUtilityIndex);

        sortList.sort((o1, o2) -> (int) (o1.getValue() - o2.getValue()));
        double e =0;
        for (int m=0;m<sortList.size();m++){
            e += Math.pow(Math.abs(sortList.get(m).getKey()-m),2);
        }

        quality=-15*Math.log((e/(Math.pow(sortList.size(),3)))+0.00001f);

        return quality;
    }

    //select
    public List<AbstractUtilitySpace> roulette(List<AbstractUtilitySpace> population,List<Double> qualityList,int popSum){
        List<AbstractUtilitySpace> populationNew = new ArrayList<>(); //nextPopulation
        List<Double> temQualityList = new ArrayList<>(qualityList);
        double maxQuality,sumQualityList=0;

//        int i=0;
//        while (i<qualityList.size()){
//            temQualityList.add(qualityList.get(i));
//            i++;
//        }
        int j = 0;
        while (j < 2) {
            double maxvalue = Collections.max(temQualityList);
            int idxMaxValue = qualityList.indexOf(maxvalue);
            populationNew.add(population.get(idxMaxValue));
            temQualityList.set(idxMaxValue, -10000.00);
            sumQualityList += maxvalue;
            j++;
        }
        int k = 0;
        while (k < (popSum - 2)) {
            double currentListK = 0;
            for (int m = 0; m < population.size(); m++) {
                currentListK  +=qualityList.get(k);
                if (currentListK  > (Math.random() * sumQualityList)) {
                    populationNew.add(population.get(m));
                    System.out.println("roulette happen right in here");

                    break;
                } else {
                    System.out.println("roulette happen wrong in here");
                }
            }
            k++;
        }
        return populationNew;

    }


}
