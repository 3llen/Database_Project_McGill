---problem3---

--- 10 nodes
--- I timed it, it took 8mins, 12sec. According to the UI it took 1mins, 6sec.

--- Counters:
--- Total records written : 28461
--- Total bytes written : 13082183

--- I put problem3-results in a .tar 
--- I didn't specify PARALLEL 1, the results were automatically put in 18 different files.

register s3n://mcgill-comp421-proj4-code/myudfs.jar

raw = LOAD 's3n://mcgill-comp421-proj4/btc-2010-chunk-000' USING TextLoader as (line:chararray);

ntriples = FOREACH raw GENERATE FLATTEN(myudfs.RDFSplit3(line)) AS (subject1:chararray,predicate1:chararray,object1:chararray);

--- filter
copy1 = FILTER ntriples BY subject1 MATCHES '.*rdfabout\\.com.*';

--- copy filtered data
copy2 = foreach copy1 generate ($0) as subject2, ($1) as predicate2, ($2) as object2;

--- join
join_copies = JOIN copy1 BY object1, copy2 BY subject2;

unique = DISTINCT join_copies;
final = ORDER unique BY predicate1;

STORE final INTO '/user/hadoop/chunk2problem3-results' using PigStorage();
