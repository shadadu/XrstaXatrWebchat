package com.xrstaxatrwebchat.wchat;

import com.xrstaxatrwebchat.wchat.Utils.CorpusProcessor;
import com.xrstaxatrwebchat.wchat.Utils.StringFmtr;
import org.apache.commons.lang3.ArrayUtils;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;


public class BotResponse {


    private Context configurationContext;
    private Double Energy;
    private Logger botResponseLogger = Logger.getLogger("BotResponseLogger");

    void setEnergy(Double energy){
        this.Energy = energy;
    }

    Double getEnergy(){
        return this.Energy;
    }


    public BotResponse(Context context, Double energy){

        this.configurationContext = context;
        this.Energy = energy;

    }


	protected String getBotResponse(String userResponse,
                                    ComputationGraph neti,
                                    CorpusProcessor corpusProcessor) {

        int samples = 1;

        Context ctx = null;
        int rowSize = -1;

        try{
            ctx = this.configurationContext;
            rowSize = (int) ctx.lookup("java:comp/env/ROW_SIZE");
            samples = (int) ctx.lookup("java:comp/env/SAMPLES");
        }catch (NamingException nme){
            nme.printStackTrace();
        }

        String line = userResponse + "\n";

        ByteArrayInputStream wordsStream = new ByteArrayInputStream
                (line.getBytes(StandardCharsets.UTF_8));

        CorpusProcessor dialogProcessor = new CorpusProcessor(wordsStream, rowSize, false);
        dialogProcessor.setDict(corpusProcessor.getDict());
        dialogProcessor.setRevDict(corpusProcessor.getRevDict());

        List<String> words = new ArrayList<>();
        dialogProcessor.tokenizeLine(userResponse, words, true, ctx);
        final List<Double> wordIdxs = dialogProcessor.wordsToIndexes(words);

        int numUnknown = 0;
        double fractionUnknown = 0.0;

        for(Double item: wordIdxs) {
        	if(item == 0.0) {
        		numUnknown += 1;
        	}
        }

        fractionUnknown = (double) numUnknown / wordIdxs.size();
        System.out.println("fraction of words unknown: "+ fractionUnknown);

        String rs = "";
        String currentResponse = "";
        if (!wordIdxs.isEmpty() && (fractionUnknown < 0.50) ) {

            rs = rs.concat(networkOutput(wordIdxs, true, neti, corpusProcessor, ctx));

//            Double optimumEnergy = this.getEnergy();
//
//            System.out.println("gen :"+ 0 +" Energy: "+this.getEnergy()+" "+rs);

//            for(int i=1; i< samples; i++ ){
//
//                currentResponse = networkOutput(wordIdxs, true, neti, corpusProcessor, ctx);
//                if(this.getEnergy() > optimumEnergy){
//                    optimumEnergy = this.getEnergy();
//                    rs = currentResponse;
//                }
//
//                System.out.println("gen :"+i+" Energy: "+this.getEnergy()+" "+currentResponse);
//
//            }


        }else {
        	rs = "Didn't get that. Could you rephrase?";
        }


        try{
            wordsStream.close();
        }catch (IOException ioe){
            botResponseLogger.info("resource already closed or does not exist");
            ioe.printStackTrace();
        }

        return rs;
    }

    private String networkOutput(List<Double> rowIn,
                                boolean printUnknowns,
                                ComputationGraph neti,
                                CorpusProcessor corpusProcessor,
                                Context initCtx) {

        neti.rnnClearPreviousState();
        Collections.reverse(rowIn);
        Map<String, Double> dicti = corpusProcessor.getDict();
        Map<Double, String> revDicti = corpusProcessor.getRevDict();

        String response = "";

        if(rowIn.size()>=1){
            INDArray in = Nd4j.create(ArrayUtils
            		.toPrimitive(rowIn.toArray(new Double[0])),
            		new int[] { 1, 1, rowIn.size() });

            double[] decodeArr = new double[dicti.size()];
            decodeArr[2] = 1;
            INDArray decode = Nd4j.create(decodeArr, new int[] { 1, dicti.size(), 1 });
            neti.feedForward(new INDArray[] { in, decode }, false, false);

            org.deeplearning4j.nn.layers.recurrent.LSTM decoder =
                    (org.deeplearning4j.nn.layers.recurrent.LSTM) neti.getLayer("decoder");

            Layer output = neti.getLayer("output");
            GraphVertex mergeVertex = neti.getVertex("merge");
            INDArray thoughtVector = mergeVertex.getInputs()[1];
            LayerWorkspaceMgr mgr = LayerWorkspaceMgr.noWorkspaces();

            INDArray activationSums = Nd4j.zeros(1, dicti.size(), 1);

            int rowSize =-1;

            try{
                rowSize = (int) initCtx.lookup("java:comp/env/ROW_SIZE");
            }catch (NamingException ne){
                ne.printStackTrace();
            }

            Random rnd = new Random(new Date().getTime());

            int numberWordsResponse = 0;


            for (int row = 0; row < rowSize; ++row) {

                mergeVertex.setInputs(decode, thoughtVector);
                INDArray merged = mergeVertex.doForward(false, mgr);
                INDArray activateDec = decoder.rnnTimeStep(merged, mgr);
                INDArray out = output.activate(activateDec, false, mgr);

                activationSums = activationSums.add(out);

                double d = rnd.nextDouble();
                double sum = 0.0;
                int idx = -1;
                for (int s = 0; s < out.size(1); s++) {

                    sum += out.getDouble(0, s, 0);
                    if (d <= sum) {
                        idx = s;
                        if (printUnknowns || s != 0) {
                            response = response.concat(revDicti.get((double) s) + " ");
                            numberWordsResponse += 1;
                        }
                        break;
                    }
                }
                if (idx == 1) {
                    break;
                }
                double[] newDecodeArr = new double[dicti.size()];
                newDecodeArr[idx] = 1;
                decode = Nd4j.create(newDecodeArr, new int[] { 1, dicti.size(), 1 });

            }

            double averageActivation = activationSums.sum(0).sum(0).getDouble(0) *100 / (dicti.size() * numberWordsResponse);

            botResponseLogger.info("averageActivation: "+ averageActivation+ " "+numberWordsResponse );

            double normalizedTotalActivations = Math
                    .log(activationSums.sum(0).sum(0).getDouble(0) *100) / (dicti.size() * numberWordsResponse );

//            double normalizedTotalActivations = Math.log(activationSums.sum(0).sum(0).getDouble(0) *100) / (dicti.size() * rowIn.size() );

            setEnergy(normalizedTotalActivations);

//            System.out.println("normalizedTotalActivations: "+normalizedTotalActivations);


        }else{
            response = "Come again?";
        }

        String fmtdResponse = StringFmtr.get(StringFmtr.truncateLine(response));
//        System.out.println("Out: "+fmtdResponse);

        return StringFmtr.get(StringFmtr.truncateLine(response));
    }


}
