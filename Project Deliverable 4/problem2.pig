register s3n://mcgill-comp421-proj4-code/myudfs.jar


-- load the test file into Pig
--raw = LOAD 's3n://mcgill-comp421-proj4/comp421-test-file' USING TextLoader as (line:chararray);
-- later you will load to other files, example:
raw = LOAD 's3n://mcgill-comp421-proj4/btc-2010-chunk-000' USING TextLoader as (line:chararray); 

-- parse each line into ntriples
ntriples = foreach raw generate FLATTEN(myudfs.RDFSplit3(line)) as (subject:chararray,predicate:chararray,object:chararray);

--group the n-triples by object column
subjects = group ntriples by (subject) PARALLEL 50;

-- flatten the objects out (because group by produces a tuple of each object
-- in the first column, and we want each object ot be a string, not a tuple),
-- and count the number of tuples associated with each object
count_by_subject = foreach subjects generate flatten($0), COUNT($1) as countval PARALLEL 50;

--order the resulting tuples by their count in descending order
group_subj_by_count = group count_by_subject by (countval);
count_subj_by_count = foreach group_subj_by_count generate flatten($0) as countval, COUNT($1) as num_subj;
count_subj_by_object_ordered = order count_subj_by_count by countval;

-- store the results in the folder /user/hadoop/example-results
 store count_subj_by_object_ordered into '/user/hadoop/histogram' using PigStorage();
-- Alternatively, you can store the results in S3, see instructions:
--store count_by_object_ordered into 's3n://dianaanika/example-results';
