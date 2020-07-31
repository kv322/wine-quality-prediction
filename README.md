# wine-quality-prediction
wine-quality-prediction is a scala application which uses hadoop spark to train and predict wine quality in a distributed environment

# To run prediction
`docker run -v "$(pwd)"/input:/input kv322/wine-quality-prediction-0.0.1`

# Note:
1.pathToTestDataSetFile from current working directory
2.Test dataset filename should be “TestDataset.csv”
