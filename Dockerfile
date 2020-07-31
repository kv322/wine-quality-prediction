FROM openjdk:8-jre-alpine

RUN pwd
RUN echo ${HOME}
RUN apk update && apk add bash
RUN apk add --no-cache curl
RUN apk update && apk add --no-cache gcompat

ENV HOME=/
ENV HADOOP_VERSION=2.8.4
ENV HADOOP_HOME=/hadoop-$HADOOP_VERSION
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH=${HADOOP_HOME}/bin:$PATH
#RUN curl -sL --retry 3 "http://archive.apache.org/dist/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz" | tar -xzvf -C /tmp/ && mv /tmp/hadoop-$HADOOP_VERSION $HADOOP_HOME  && rm -rf $HADOOP_HOME/share/doc
RUN wget "http://archive.apache.org/dist/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz"
RUN tar -xvf hadoop-$HADOOP_VERSION.tar.gz
RUN rm hadoop-$HADOOP_VERSION.tar.gz

RUN echo ${HOME}
RUN echo "after hadoop"

ENV SPARK_VERSION=2.4.0
ENV SPARK_PACKAGE=spark-${SPARK_VERSION}-bin-without-hadoop
ENV SPARK_HOME=/spark-${SPARK_VERSION}-bin-without-hadoop
RUN echo "${SPARK_HOME}"

#RUN echo "$(hadoop)"
#ENV SPARK_DIST_CLASSPATH="$(hadoop\ classpath)"
ENV SPARK_DIST_CLASSPATH="/hadoop-2.8.4/etc/hadoop:/hadoop-2.8.4/share/hadoop/common/lib/*:/hadoop-2.8.4/share/hadoop/common/*:/hadoop-2.8.4/share/hadoop/hdfs:/hadoop-2.8.4/share/hadoop/hdfs/lib/*:/hadoop-2.8.4/share/hadoop/hdfs/*:/hadoop-2.8.4/share/hadoop/yarn/lib/*:/hadoop-2.8.4/share/hadoop/yarn/*:/hadoop-2.8.4/share/hadoop/mapreduce/lib/*:/hadoop-2.8.4/share/hadoop/mapreduce/*:/hadoop-2.8.4/contrib/capacity-scheduler/*.jar"

#RUN echo "${SPARK_HOME}"
#RUN echo "${SPARK_DIST_CLASSPATH}"
#RUN echo "$(hadoop classpath)"

ENV PATH=${SPARK_HOME}/bin:$PATH
# RUN curl -sL --retry 3 "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz" | gunzip | tar x -C /tmp/   && mv /tmp/$SPARK_PACKAGE $SPARK_HOME
RUN wget "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz"
RUN tar -xvf ${SPARK_PACKAGE}.tgz
RUN rm ${SPARK_PACKAGE}.tgz

RUN echo "$(pwd)"
WORKDIR "spark-${SPARK_VERSION}-bin-without-hadoop"
#RUN echo "$(pwd)"
WORKDIR "jars"

RUN wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-s3/1.11.95/aws-java-sdk-s3-1.11.95.jar
RUN wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-core/1.11.95/aws-java-sdk-core-1.11.95.jar
RUN wget https://repo1.maven.org/maven2/org/apache/spark/spark-hive_2.11/2.4.0/spark-hive_2.11-2.4.0.jar
RUN wget https://repo1.maven.org/maven2/com/databricks/spark-csv_2.11/1.5.0/spark-csv_2.11-1.5.0.jar
RUN wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.2/httpclient-4.5.2.jar
RUN wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-client/2.8.4/hadoop-client-2.8.4.jar
RUN wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-common/2.8.4/hadoop-common-2.8.4.jar
RUN wget https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.8.4/hadoop-aws-2.8.4.jar
RUN wget https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk/1.11.95/aws-java-sdk-1.11.95.jar

#RUN echo "$(ls -ltr)"
RUN echo "$(pwd)"

WORKDIR "../bin"

RUN echo "$(pwd)"
#RUN echo "$(ls -ltr)"
RUN echo "${PATH}"
#RUN CURL -f -L -o /wine-quality-prediction.jar "https://cs-643-850-assignment-2.s3.amazonaws.com/JAR/wine-quality-prediction-assembly-1.0.jar"
#CMD ./spark-submit --class demo.common.WineQualityModelTrain \
#    s3a://cs-643-850-assignment-2/JAR/wine-quality-prediction_2.11-1.0.jar \
#    --master=local[*] --run-type=prediction \
#    --model-store-path=s3a://cs-643-850-assignment-2/output/model \
#    --testing-file-path=s3a://cs-643-850-assignment-2/input/validation/ValidationDataset.csv
CMD ./spark-submit --class demo.common.WineQualityModelTrain \
    s3a://cs-643-850-assignment-2/JAR/wine-quality-prediction_2.11-1.0.jar \
    --master=local[*] --run-type=prediction \
    --model-store-path=s3a://cs-643-850-assignment-2/output/model \
    --testing-file-path=file:///input/TestDataset.csv
