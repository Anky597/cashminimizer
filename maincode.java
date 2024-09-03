import java.util.*;

class Bank {
    String name;
    int netAmount;
    Set<String> types;

    public Bank() {
        types = new HashSet<>();
    }
}

public class CashFlowMinimizer {
    
    static int getMinIndex(Bank[] listOfNetAmounts, int numBanks) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    static int getSimpleMaxIndex(Bank[] listOfNetAmounts, int numBanks) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int numBanks, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < 0) continue;

            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }

        return new Pair<>(maxIndex, matchingType);
    }

    static void printAns(List<List<Pair<Integer, String>>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows : \n");

        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                if (ansGraph.get(i).get(j).first != 0 && ansGraph.get(j).get(i).first != 0) {
                    if (ansGraph.get(i).get(j).first.equals(ansGraph.get(j).get(i).first)) {
                        ansGraph.get(i).get(j).first = 0;
                        ansGraph.get(j).get(i).first = 0;
                    } else if (ansGraph.get(i).get(j).first > ansGraph.get(j).get(i).first) {
                        ansGraph.get(i).get(j).first -= ansGraph.get(j).get(i).first;
                        ansGraph.get(j).get(i).first = 0;

                        System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).first + " to " + input[j].name + " via " + ansGraph.get(i).get(j).second);
                    } else {
                        ansGraph.get(j).get(i).first -= ansGraph.get(i).get(j).first;
                        ansGraph.get(i).get(j).first = 0;

                        System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).first + " to " + input[i].name + " via " + ansGraph.get(j).get(i).second);
                    }
                } else if (ansGraph.get(i).get(j).first != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).first + " to " + input[j].name + " via " + ansGraph.get(i).get(j).second);
                } else if (ansGraph.get(j).get(i).first != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).first + " to " + input[i].name + " via " + ansGraph.get(j).get(i).second);
                }

                ansGraph.get(i).get(j).first = 0;
                ansGraph.get(j).get(i).first = 0;
            }
        }
    }

    static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, List<List<Integer>> graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank();
            listOfNetAmounts[b].name = input[b].name;
            listOfNetAmounts[b].types = input[b].types;

            int amount = 0;
            for (int i = 0; i < numBanks; i++) {
                amount += graph.get(i).get(b);
            }
            for (int j = 0; j < numBanks; j++) {
                amount -= graph.get(b).get(j);
            }

            listOfNetAmounts[b].netAmount = amount;
        }

        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            ansGraph.add(new ArrayList<>(Collections.nCopies(numBanks, new Pair<>(0, ""))));
        }

        int numZeroNetAmounts = 0;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) numZeroNetAmounts++;
        }

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts, numBanks);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, numBanks, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.first;

            if (maxIndex == -1) {
                ansGraph.get(minIndex).get(0).first += Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(minIndex).get(0).second = input[minIndex].types.iterator().next();

                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts, numBanks);
                ansGraph.get(0).get(simpleMaxIndex).first += Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(0).get(simpleMaxIndex).second = input[simpleMaxIndex].types.iterator().next();

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);

                ansGraph.get(minIndex).get(maxIndex).first += transactionAmount;
                ansGraph.get(minIndex).get(maxIndex).second = maxAns.second;

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in the different corners of the world that use different modes of payment.There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment. \n\n");
        System.out.println("Enter the number of banks participating in the transactions.");
        int numBanks = sc.nextInt();

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has, and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces");

        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            input[i] = new Bank();

            if (i == 0) {
                System.out.print("World Bank : ");
            } else {
                System.out.print("Bank " + i + " : ");
            }
            input[i].name = sc.next();
            indexOf.put(input[i].name, i);

            int numTypes = sc.nextInt();
            if (i == 0) {
                maxNumTypes = numTypes;
            }
            for (int j = 0; j < numTypes; j++) {
                input[i].types.add(sc.next());
            }
        }

        System.out.println("Enter the number of transactions and the details:");
        System.out.println("Transaction details should contain the name of the giver bank, the name of the taker bank, the amount of transaction, and the type of transaction.");

        int numTransactions = sc.nextInt();
        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            graph.add(new ArrayList<>(Collections.nCopies(numBanks, 0)));
        }

        for (int i = 0; i < numTransactions; i++) {
            String giverBank = sc.next();
            String takerBank = sc.next();
            int amount = sc.nextInt();
            String transactionType = sc.next();

            if (input[indexOf.get(giverBank)].types.contains(transactionType) && input[indexOf.get(takerBank)].types.contains(transactionType)) {
                graph.get(indexOf.get(giverBank)).set(indexOf.get(takerBank), graph.get(indexOf.get(giverBank)).get(indexOf.get(takerBank)) + amount);
            } else if (!input[0].types.contains(transactionType)) {
                continue;
            } else {
                graph.get(indexOf.get(giverBank)).set(0, graph.get(indexOf.get(giverBank)).get(0) + amount);
                graph.get(0).set(indexOf.get(takerBank), graph.get(0).get(indexOf.get(takerBank)) + amount);
            }
        }

        minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, maxNumTypes);
    }

    static class Pair<F, S> {
        F first;
        S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}
