# Fast Denial Constraint Discovery (FDCD)
This repository contains the code for the paper "Fast Algorithms for Denial Constraint Discovery" submitted to VLDB22.

## Installation
Before building the algorithms, the following prerequisites need to be installed:
* Java JDK 1.8 or later
* Maven 3.1.0 or later
* Git
* Boost (only for enumeration with the MMCS algorithm)

## Setup FDCD

### Clone the code
As the first step, clone this repository :
```bash
$ git clone https://github.com/EduardoPena/fdcd.git
$ cd fdcd
```
### Install 
Then, build fdcd with the following command:
```bash
.../fdcd$ mvn clean install
```
The command above will create a "fat" jar called discoverDCs.jar and place it into the target folder.
### MMCS Algorithm 
DC enumeration with the MMCS algorithm requires a C++ implementation, found in: [MHS generation algorithms](https://github.com/VeraLiconaResearchGroup/Minimal-Hitting-Set-Algorithms/blob/master/README.md).
Please, follow the instructions to build the executable (we use the default name, agdmhs). Then, copy the executable agdmhs into the folder containg the fdcd jar (e.g., target).

### Repository structure
--------------------
*  `src/`: the Java implementation of fdcd
*  `data/`: the datasets used for experiments

## Running the code
Once you have compiled the code, you can run the discovery, for example:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv
```
### Parameters
The only required parameter is the dataset, see the `data/` folder for how .csv are specified.
You can also specify three additional parameters:
- `-n` : the number of rows to be read as input for the algorithms. For example:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000
```
- `-o` : A path to the output file. For example:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000 -o taxdcs.out
```
In case the parameter -o is not specified, the program only shows the number of results.

- `-e` : enumeration method  (INCS, EI, HEI, MMCS, HMMCS, MCS). Default is INCS . For example:
```bash
.../fdcd$ java -jar target/discoverDCs.jar data/tax.csv -n 10000 -o taxdcs.out -e HEI
```

