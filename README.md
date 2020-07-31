# wine-quality-prediction
wine-quality-prediction is a scala application which uses hadoop spark to train and predict wine quality in a distributed environment

## To run prediction (with docker)
`docker run -v "$(pwd)"/input:/input kv322/wine-quality-prediction-0.0.1`

### Note:
1. In above command "$(pwd)"**/input** is path to TestDataset file from current working directory
2. Test dataset filename should be “TestDataset.csv”

## To run prediction (without docker)

Goto spark2.4.0/bin directory and run below command

./spark-submit --class demo.common.WineQualityModelTrain  s3a://cs-643-850-assignment-2/JAR/wine-quality-prediction_2.11-1.0.jar --master=local[*] --run-type=prediction 
--model-store-path=s3a://cs-643-850-assignment-2/output/model --testing-file-path=s3a:///"$(pwd)"**/input/TestDataset.csv**

### Note:
Sample directory and path from current working directory

### For more information please see README_FULL.docx for steps and sample output
