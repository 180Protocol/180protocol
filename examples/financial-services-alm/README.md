# 180P Use Case: Asset Liability mismatch use case for creating financial asset marketplace insights

For detailed instructions on how to set up and customize CoApps like this using the 180Protocol SDKs, please see

* [Writing your 180protocol CoApp](https://docs.180protocol.com/develop/tutorials/writing-your-180protocol-coapp)

## Business case

This CoApp is a representative application built on the Aggregator SDK. The use case involves a coalition of asset managers
who have come together to generate unique and tangible portfolio insights from their private cash flow data sets. The data providers
are major asset managers sharing their asset-liability cash flow profiles. The output of the data aggregation is the Net ALM mismatch data based
on cash flows from individual providers. 

Data Providers - Financial asset owners (institutional) that hold assets to generate return
Data Consumers - Financial asset brokers/dealers or other intermediaries selling financial assets to institutions

## Implementation details

ALM mismatch

•	A and L curves are inherently sensitive data sets owned by asset owners - providers
•	A and L curves are netted to construct demand curves
•	Demand curves are input data shared to enclaves
•	Enclaves compute output ALM curves
•	Enclave calculates rewards

Sample input data

For Year 1

| Liability (m) | liabilityDiscounted | Assets  | Assets risk adjusted  | Net A/L (cash basis)| Net A/L (discounted basis)|
|-------------|-----------------|-----------|-------------|-----------|---------------|
50 | 50 | 550 | 550 | 500 | 500 |
70 | 70 | 770 | 770 | 700 | 700 |
90 | 90 | 990 | 990 | 900 | 900 |
110 | 110 | 1210 | 1210 | 1100 | 1100 |
130 | 130 | 1430 | 1430 | 1300 | 1300 |
150 | 150 | 1650 | 1650 | 1500 | 1500 |

For Year 2

| Liability (m) | liabilityDiscounted | Assets  | Assets risk adjusted  | Net A/L (cash basis)| Net A/L (discounted basis)|
|-------------|-----------------|-----------|-------------|-----------|---------------|
50 | 50 | 550 | 550 | 500 | 500 |
70 | 70 | 770 | 770 | 700 | 700 |
90 | 90 | 990 | 990 | 900 | 900 |
110 | 110 | 1210 | 1210 | 1100 | 1100 |
130 | 130 | 1430 | 1430 | 1300 | 1300 |
150 | 150 | 1650 | 1650 | 1500 | 1500 |

### Provider Rewards
Data Provider rewards are calculated for each data provider (in this case car manufacturers) by the enclave based aggregator. 
Rewards are based on several individual factors that may differ for each use case and can be configured. 
For this use case, the factors are:

* Amount Provided – Percentage of rows from the data provider out of the entire data set from all providers
* Uniqueness – How much of the ALM data is 1 Standard Deviation from the mean
* Completeness – How many years of complete ALM data is provided
* Update Frequency – Frequency of updates of the data over past few months

e.x.,
```JSON
{
	"amountProvided": 0.5,
	"completeness": 0.5,
	"uniqueness": 0.6666667,
	"updateFrequency": 0.71428573,
	"qualityScore": 0.5952381,
	"rewards": 59.523808,
	"dataType": "testSchema1"
}
```
To summarize, an asset manager providing the most ALM data, that covers a wide range of years and has a different cash flow profile from 
its peers, gets rewarded the most.
