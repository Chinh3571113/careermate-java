package com.fpt.careermate.services.coach_services.service;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.LongBuffer;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingImp {
    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            env = OrtEnvironment.getEnvironment();

            var tokenizerStream = getClass().getResourceAsStream("/onnx/tokenizer.json");
            if (tokenizerStream == null) {
                throw new RuntimeException("tokenizer.json not found in resources");
            }
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerStream, Map.of());

            try (var modelStream = getClass().getResourceAsStream("/onnx/model.onnx")) {
                if (modelStream == null) {
                    throw new RuntimeException("model.onnx not found in resources");
                }
                byte[] modelBytes = modelStream.readAllBytes();
                session = env.createSession(modelBytes);
            }

            log.info("ONNX model loaded successfully");
        } catch (Exception e) {
            log.error("ONNX model error: "+e.getMessage(), e);
        }
    }

    public float[] embed(String text) {
        try {
            // 1. Encode
            var encoding = tokenizer.encode(text);

            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds = encoding.getTypeIds();

            // 2. Create tensors
            try (
                    OnnxTensor inputIdsTensor = OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(inputIds),
                            new long[]{1, inputIds.length}
                    );
                    OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(attentionMask),
                            new long[]{1, attentionMask.length}
                    );
                    OnnxTensor tokenTypeIdsTensor = OnnxTensor.createTensor(
                            env,
                            LongBuffer.wrap(tokenTypeIds),
                            new long[]{1, tokenTypeIds.length}
                    )
            ) {
                Map<String, OnnxTensor> inputs = Map.of(
                        "input_ids", inputIdsTensor,
                        "attention_mask", attentionMaskTensor,
                        "token_type_ids", tokenTypeIdsTensor
                );

                // 3. Run inference
                try (OrtSession.Result result = session.run(inputs)) {

                    // output shape: [1, seq_len, hidden]
                    float[][][] tokenEmbeddings =
                            (float[][][]) result.get(0).getValue();

                    float[] sentenceEmbedding =
                            meanPooling(tokenEmbeddings[0], attentionMask);

                    return normalize(sentenceEmbedding);
                }
            }
        } catch (Exception e) {
            log.error("Embedding error: "+e.getMessage(), e);
            return new float[0]; // Return empty array on error
        }
    }

    public double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private float[] normalize(float[] vector) {
        double norm = 0.0;
        for (float v : vector) norm += v * v;
        norm = Math.sqrt(norm);

        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
        return vector;
    }

    private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
        int dim = tokenEmbeddings[0].length;
        float[] sentenceEmbedding = new float[dim];
        int count = 0;

        for (int i = 0; i < attentionMask.length; i++) {
            if (attentionMask[i] == 1) {
                count++;
                for (int j = 0; j < dim; j++) {
                    sentenceEmbedding[j] += tokenEmbeddings[i][j];
                }
            }
        }

        if (count > 0) {
            for (int j = 0; j < dim; j++) {
                sentenceEmbedding[j] /= count;
            }
        }

        return sentenceEmbedding;
    }


}
