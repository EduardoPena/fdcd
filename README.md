# Fast Denial Constraint Discovery (FDCD)
This repository contains the code for the paper "Fast Algorithms for Denial Constraint Discovery" submitted to VLDB22.

--------------------

## Installation dependencies
Before building the algorithms, make sure to install the following prerequisites:
* Java JDK 1.8 or later
* Maven 3.1.0 or later
* Git
* Boost (only for enumeration with the MMCS algorithm)

--------------------

## Setup 

### 1. Clone the code
As the first step, clone this repository :
```bash
$ git clone https://github.com/EduardoPena/fdcd.git
$ cd fdcd
```
### 2. Compile the code and generate jar file 
Then, build fdcd with the following maven command:
```bash
.../fdcd$ mvn clean install
```
The command above will create a "fat" jar called discoverDCs.jar and place it into the target folder.
### 3. Install MMCS Algorithm (optional)
DC enumeration with the MMCS algorithm requires a C++ implementation, found in: [MHS generation algorithms](https://github.com/VeraLiconaResearchGroup/Minimal-Hitting-Set-Algorithms/blob/master/README.md).
If you are willing to use it, please, follow the instructions to build the executable (we use the default name, agdmhs). Then, copy the executable agdmhs into the folder containg the fdcd jar (e.g., target).
--------------------

## Execution
Once you have compiled the code, you can run the discovery, for example:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv
```
### Parameters
The only required parameter is the dataset. See the `data/` folder for sample .csv files.
Additionally, you can specify three optional parameters:
- `-n` : number of rows. For example, the following command executes the discovery with the first 10000 rows of the dataset:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000
```


- `-o` : output file path. In case the parameter -o is not specified, the program only shows the number of results. The following command saves the discovered DCs in the *taxdcs.out* file.
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000 -o taxdcs.out
```


- `-e` : enumeration method. The enumeration method to be used with the ECP algorithm. The following algorithms are available: INCS, EI, HEI, MMCS, HMMCS, MCS (check the paper for technical details). The  default is INCS. For example, the following command runs the discovery using the HEI enumeration algorithm:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000 -o taxdcs.out -e HEI
```
## Repository structure

*  `src/`: the Java implementation of fdcd
*  `data/`: a sample of the datasets used for experiments

--------------------
## More data
This repository contains only sample datasets.
The full datasets used in the paper are hosted [here](https://owncloud.hpi.de/s/PBs9ME6HrsZPe9Z)

--------------------
## Metanome and comparisons
We compare our algorithms with state-of-the-art algorithms found [here](https://hpi.de/naumann/projects/repeatability/data-profiling/metanome-dc-algorithms.html)
These algorithms are integrated with [Metanome](https://hpi.de/naumann/projects/data-profiling-and-analytics/metanome-data-profiling.html), a specialized data profiling plataform.
We intend to integrate our algorithm into the plataform soon.

--------------------



