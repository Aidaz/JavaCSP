package balcaitisa_l3b;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import org.jcsp.lang.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class Globals
{
    public static final int productsSize = 100;
    public static int ConsumersSize = 5, 
            ProducersSize = 5, 
            finishedProducers = 0, 
            finishedConsumers = 0;
    public static final String dataFile = "BalcaitisA_L3b_dat_1.txt"; // Pasalinami visi
    //public static final String dataFile = "BalcaitisA_L3b_dat_2.txt"; // Nepasalina nei vieno
    //public static final String dataFile = "BalcaitisA_L3b_dat_3.txt"; // Pasalina tik dali
    public static final String resultsFile = "BalcaitisA_L3b_res.txt";
    public static boolean HaveProducersFinished(){
        return finishedProducers  == ProducersSize;
    }
    public static boolean HaveConsumersFinished(){
        return finishedConsumers  == ConsumersSize;
    }
}
//Action types for channels' communaction
enum ActionType {
    insert, remove, producerFinished, consumerFinished
}
//Class for keeping transfering data via channels
class Action{
   public Object data;
   public ActionType actionType;
   public int processID = -1;
}

/*
* Class contains producer type products
*/
class ProducerProducts
{
   public String product;
   public int count;
   public double price;
   public ProducerProducts()
   {
       product = "";
       count = 0;
       price = 0.0;
   }
}
/*
* Class contains consumer type products
*/
class ConsumerProducts
{
   public String product;
   public int count;
   public ConsumerProducts()
   {
       product = "";
       count = 0;
   }
}

/*
* Common array class
*/
class Buffer
{
   private ConsumerProducts[] B = new ConsumerProducts[Globals.productsSize];
   private int size;

   public Buffer()
   {
       for (int i = 0; i < Globals.productsSize; i++)
       {
           B[i] = new ConsumerProducts();
       }
       size = 0;
   }

   /**
     * Returns product name at index location
     * @param index product location in common array
     */
   public ConsumerProducts[] getItems() { return B; }

   /**
     * Increments finished producers count
     */
   public int getSize() { return size; }

   /**
     * Checks if product exists in common array
     * @param name product name
     */
   public int ifExists(String name)
   {

       int index = -1;
       for (int i = 0; i < size; i++)
       {
           if (name.compareTo(B[i].product) == 0) //if finds in array
           {
               index = i;
               break;
           }
       }
       return index;
   }

   public void WriteToArray(ConsumerProducts product)
   {
       int index = ifExists(product.product); 
       if (index == -1)
        {
            index = RequestInsertIndex(product.product);
            insert(product.product, product.count, index);
        }
        else
        {
            B[index].count += product.count;
        }
   }
   // Grąžina, kiek masyve yra elementų.
   public ConsumerProducts ReadFromArray(ConsumerProducts product){
        ConsumerProducts temp = new ConsumerProducts();
        temp.product = product.product;
        temp.count = product.count;
        int index = ifExists(temp.product);
        if(index != -1){
            if (B[index].count <= temp.count){
                temp.count = temp.count - B[index].count;
                B[index].count = 0;
            }
            if (B[index].count > temp.count){
                B[index].count -= temp.count;
                temp.count = 0;
            }
            if (B[index].count == 0){
                remove(index);
            }
        }
        return temp;
   }

   /**
     * Removes product from common array at index location
     * @param index product index in common array
     */
   public void remove(int index)
   {
       if (index == size - 1)
       {
           B[index].product = "";
           B[index].count = 0;
       }
       else
       {
           for (int i = index; i < size; i++)
           {
               B[i] = B[i + 1];
           }
           B[size - 1].product = "";
           B[size - 1].count = 0;
       }
       --size;
   }
   /**
     * Inserts product to index location in common array
     * @param name product name
     * @param index index to where insert product
     */
   public void insert(String name, int count, int index)
   {
       ConsumerProducts temp = new ConsumerProducts();
       temp.product = name;
       temp.count = count;
       size++;
       if (size == 1)
       {
           B[index] = temp;
       }
       else
       {
           for (int i = size; i > index + 1; i--)
           {
               B[i - 1] = B[i - 2];
           }
           B[index] = temp;
       }
   }

   /**
     * Gets product index from common array
     * @param name product name
     */
   public int RequestInsertIndex(String name)
   {
       int index = -1;
       if (size == 0) return 0;
       if (size == 1)
           if (name.compareTo(B[0].product) > 0)
               return 1;
           else return 0;
       if (name.compareTo(B[size - 1].product) > 0)
           return size;
       for (int i = 0; i < size; i++)
       {
           if (name.compareTo(B[i].product) < 0)
           {
               index = i;
               return index;
           }
       }
       return index;
   }
}

class Procesas_00 implements CSProcess {
   private ArrayList< ArrayList<ProducerProducts> > P; //Producers' data
   private ArrayList< ArrayList<ConsumerProducts> > C; //Consumers' data
   private ArrayList<ChannelOutput> ProducersChannels; //Channels for communicating with producers
   private ArrayList<ChannelOutput> ConsumersChannels; //Channels for communicating with consumers
   private ChannelInput ResultsChannel; //Channel for receiving final array for printing

   public Procesas_00(ArrayList<ChannelOutput> ProducersChannels,
           ArrayList<ChannelOutput> ConsumersChannels,
           ChannelInput ResultsChannel)
   {
       this.P = new ArrayList<>();
       this.C = new ArrayList<>();
       this.ProducersChannels = ProducersChannels;
       this.ConsumersChannels = ConsumersChannels;
       this.ResultsChannel = ResultsChannel;
   }

   @Override
   public void run(){       
       try {
           //Gets data from file
           BalcaitisA_L3b.Read(P, C);
       } catch (Exception ex) {
           Logger.getLogger(BalcaitisA_L3b.class.getName()).log(Level.SEVERE, null, ex);
       }
       try {
           //Writes original data to results file
           BalcaitisA_L3b.writeTables(P, C);
       } catch (Exception ex) {
           Logger.getLogger(BalcaitisA_L3b.class.getName()).log(Level.SEVERE, null, ex);
       }
       //Distributes data to all producers
       for(int i = 0; i < P.size(); i++){
           ProducersChannels.get(i).write(P.get(i));
       }
        //Distributes data to all consumers
       for(int i = 0; i < C.size(); i++){
           ConsumersChannels.get(i).write(C.get(i));
       }
       //Reads final array data from manager process
       Buffer B = (Buffer) ResultsChannel.read();
       try {
           //Prints final array to results file
           BalcaitisA_L3b.writeResults(B);
       } catch (Exception ex) {}
       System.out.println("Distributor darbas baigtas");
   }
}

class Consumer implements CSProcess {
   private int processID; //saves consumer's process id
   private ArrayList<ConsumerProducts> products = new ArrayList<ConsumerProducts>(); //Saves which data to consume
   private ChannelInput inputDataChannel,  //Channel to get data from procesas_00
           ConsumeResultChannel; //Channel to get info from manager about successful read from array
   private ChannelOutput outputChannel; //Channel with manager to consume items

   public Consumer(ChannelInput inputDataChannel, 
           ChannelOutput outputChannel, ChannelInput ConsumeResultChannel, int processID){
       this.inputDataChannel = inputDataChannel;
       this.outputChannel = outputChannel;
       this.ConsumeResultChannel = ConsumeResultChannel;
       this.processID = processID;
   }

   @Override
   public void run(){
       //Reads data from procesas_00
       products = (ArrayList<ConsumerProducts>) inputDataChannel.read();
       int result, i = 0;
       while(products.size() > 0)
       {
           Action action = new Action();
           action.actionType = ActionType.remove;
           ConsumerProducts consProducts = new ConsumerProducts();
           consProducts.product = products.get(i).product;
           consProducts.count = products.get(i).count;
           action.data = consProducts;
           action.processID = processID;
           outputChannel.write(action);
           result = (int) ConsumeResultChannel.read();
           if(result == 0){
               products.remove(i);
           }
           else if (!Globals.HaveProducersFinished()){
               products.get(i).count = result;
           }
           else products.remove(i);
               
           if(i >= products.size()-1)
               i=0;
           else i++;
       }
       Action actionFinished = new Action();
       actionFinished.actionType = ActionType.consumerFinished;
       outputChannel.write(actionFinished);
   }
}
//Producer class
class Producer implements CSProcess {
   private static ArrayList<ProducerProducts> products; //Saves which data to produce
   private ChannelInput inputDataChannel; //Channel to get data from procesas_00
   private ChannelOutput outputChannel; //Channel with manager to produce items

   public Producer(ChannelInput inputDataChannel, 
           ChannelOutput outputChannel){
       this.inputDataChannel = inputDataChannel;
       this.outputChannel = outputChannel;
   }

   @Override
   public void run(){
       //Reads data from procesas_00
       products = (ArrayList<ProducerProducts>) inputDataChannel.read();
       for(ProducerProducts prod : products){
           Action action = new Action();
           action.actionType = ActionType.insert;
           ConsumerProducts consProducts = new ConsumerProducts();
           consProducts.product = prod.product;
           consProducts.count = prod.count;
           action.data = consProducts;
           outputChannel.write(action);
       }
       Action actionFinished = new Action();
       actionFinished.actionType = ActionType.producerFinished;
       outputChannel.write(actionFinished);
   }
}

class Manager implements CSProcess{
   private ChannelOutput outputChannel; //Channel for sending data to procesas_00
   private ArrayList<ChannelOutput> ConsumeResultChannels;//Channel for communicating with consumer about successful read from data array
   private Buffer B; //Common data array
   private ArrayList<AltingChannelInput> ManagerProducerInputChannels; //List of producers' one2one input channels
   private ArrayList<AltingChannelInput> ManagerConsumerInputChannels; //List of consumers' one2one input channels

   public Manager(ChannelOutput outputChannel,
           ArrayList<ChannelOutput> ConsumeResultChannels, 
           ArrayList<AltingChannelInput> ManagerProducerInputChannels,
           ArrayList<AltingChannelInput> ManagerConsumerInputChannels){
       this.B = new Buffer();
       this.ConsumeResultChannels = new ArrayList<>(); 
       this.ManagerProducerInputChannels = new ArrayList<>();
       this.ManagerProducerInputChannels = new ArrayList<>();
       this.outputChannel = outputChannel;
       this.ConsumeResultChannels = ConsumeResultChannels;
       this.ManagerProducerInputChannels = ManagerProducerInputChannels;
       this.ManagerConsumerInputChannels = ManagerConsumerInputChannels;
   }
   
   @Override
   public void run(){
       //Guard saves all incoming channels into array
       Guard[] g = new Guard[Globals.ConsumersSize + Globals.ProducersSize];
       for(int i = 0; i < ManagerProducerInputChannels.size() 
               + ManagerConsumerInputChannels.size(); i++){
           if (i < ManagerProducerInputChannels.size()) 
               g[i] = ManagerProducerInputChannels.get(i);
           else 
               g[i] = ManagerConsumerInputChannels.get(i - Globals.ProducersSize);
       }
       //Alternative arbitrary selects from which channel to read data
       final Alternative alt = new Alternative(g);
       int index;
       
       while(!(Globals.HaveConsumersFinished() && Globals.HaveProducersFinished()))
       {
           index = alt.select();
           Action action;
           if (index < Globals.ProducersSize)
                action = (Action) ManagerProducerInputChannels.get(index).read();
           else
               action = (Action) ManagerConsumerInputChannels.get(index - Globals.ProducersSize).read();
           if(action.actionType == ActionType.insert){
               B.WriteToArray((ConsumerProducts) action.data);
           }

           if(action.actionType == ActionType.remove){
               ConsumerProducts temp = new ConsumerProducts();
               temp = B.ReadFromArray((ConsumerProducts) action.data);
               Action result = new Action();
               result.data = temp;
               ConsumeResultChannels.get(action.processID).write(temp.count);
           }

           if(action.actionType == ActionType.producerFinished){
               Globals.finishedProducers++;
               System.out.println("Producer finished");
           }

            if(action.actionType == ActionType.consumerFinished){
               Globals.finishedConsumers++;
               System.out.println("Consumer finished");
           }
           
       }
       System.out.println("Manager darbas baigtas");
       //sends final data to procesas_00
       outputChannel.write(B);
   }
}   

/**
 *
 * @author Aidas
 */
public class BalcaitisA_L3b {
    public static void main(String[] args) throws Exception {
        One2OneChannel ManagerDistributorChannel = Channel.one2one(); //Channel for manager and procesas_00 communication
        ArrayList<One2OneChannel> Procesas00ProducerChannels = new ArrayList<>(); //Channels for procesas_00 and producers communication
        ArrayList<One2OneChannel> Procesas00ConsumerChannels = new ArrayList<>(); //Channels for procesas_00 and consumers communication
        
        ArrayList<One2OneChannel> ManagerProducerChannels = new ArrayList<>(); //Channels for manager and producers communication
        ArrayList<One2OneChannel> ManagerConsumerChannels = new ArrayList<>(); //Channels for manager and consumers communication
        ArrayList<One2OneChannel> ConsumerResultChannels = new ArrayList<>(); // Channels for manager communication with consumers
        ArrayList<ChannelOutput> Procesas00ProducerOutputChannels = new ArrayList<>(); //Output channels for procesas_00 and producers communication
        ArrayList<ChannelOutput> Procesas00ConsumerOutputChannels = new ArrayList<>(); //Output channels for procesas_00 and consumers communication
        ArrayList<ChannelOutput> ConsumerResultOutputChannels = new ArrayList<>(); // Output channels for manager communication with consumers
        
        ArrayList<AltingChannelInput> ManagerProducerInputChannels = new ArrayList<>(); // Manager channels for getting data from producers
        ArrayList<AltingChannelInput> ManagerConsumerInputChannels = new ArrayList<>(); // Manager channels for getting data from consumers
        
        ArrayList<CSProcess> process = new ArrayList<>(); //List for processes to be executed
        //Initializing producers and attaching channels to them
        for(int i = 0; i < Globals.ProducersSize; i++){
            One2OneChannel channel = Channel.one2one();
            Procesas00ProducerChannels.add(channel);
            One2OneChannel ManagerProducerChannel = Channel.one2one();
            ManagerProducerChannels.add(ManagerProducerChannel);
            Producer producer = new Producer(channel.in(), ManagerProducerChannel.out());
            process.add(producer);
        }
        //Initializing consumers and attaching channels to them
        for(int i = 0; i < Globals.ConsumersSize; i++){
            One2OneChannel channel = Channel.one2one();
            Procesas00ConsumerChannels.add(channel);
            One2OneChannel resultChannel = Channel.one2one(); // Create result channel to receive data from Manager
            ConsumerResultChannels.add(resultChannel);
            One2OneChannel ManagerConsumerChannel = Channel.one2one();
            ManagerConsumerChannels.add(ManagerConsumerChannel);
            int processID = i;
            Consumer consumer = new Consumer(channel.in(), ManagerConsumerChannel.out(), resultChannel.in(), processID);
            process.add(consumer);
        }
        //Creating list of consumer and manager result output channels
        ConsumerResultChannels.stream().forEach((resultChannel) -> {
            ConsumerResultOutputChannels.add(resultChannel.out());
        });
        //Creating list of producer and manager output channels
        ManagerProducerChannels.stream().forEach((ManagerProducerChannel) -> {
            ManagerProducerInputChannels.add(ManagerProducerChannel.in());
        });
        //Creating list of consumer and manager output channels
        ManagerConsumerChannels.stream().forEach((ManagerConsumerChannel) -> {
            ManagerConsumerInputChannels.add(ManagerConsumerChannel.in());
        });
        //Initializing manager
        Manager manager = new Manager(ManagerDistributorChannel.out(), 
                ConsumerResultOutputChannels,
                ManagerProducerInputChannels, ManagerConsumerInputChannels);
        process.add(manager);
        //Creating list of procesas00 and producers output channels
        Procesas00ProducerChannels.stream().forEach((channel) -> {
            Procesas00ProducerOutputChannels.add(channel.out());
        });
        //Creating list of procesas00 and consumers output channels
        Procesas00ConsumerChannels.stream().forEach((channel) -> {
            Procesas00ConsumerOutputChannels.add(channel.out());
        });
        //Initializing procesas_00
        Procesas_00 distributor = new Procesas_00(Procesas00ProducerOutputChannels,
        Procesas00ConsumerOutputChannels, ManagerDistributorChannel.in());
        process.add(distributor);
        
        Parallel parallel = new Parallel();
        parallel.addProcess(process.toArray(new CSProcess[process.size()]));
        parallel.run();
        System.out.println("Darbas baigtas!");
    }
    
    /**
     * Reads one team from file
     * @param br data file reader buffer
     * @param team team's object
     * @throws Exception 
     */
    static void Read(ArrayList< ArrayList<ProducerProducts>> P, 
            ArrayList< ArrayList<ConsumerProducts>> C) throws Exception{
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(Globals.dataFile))) {
            if ((line = br.readLine()) != null) {
                int ProducersSize = 0, ConsumersSize = 0, size = 0;
                String[] splitString = line.split("\\s+");
                ProducersSize = Integer.parseInt(splitString[0]);
                for(int i = 0; i < Globals.ProducersSize; i++){
                    line = br.readLine();
                    splitString = line.split("\\s+");
                    ArrayList<ProducerProducts> tempProducer = new ArrayList<>();
                    size = Integer.parseInt(splitString[1]);
                    for (int j = 0; j < size; j++){
                        line = br.readLine();
                        splitString = line.split("\\s+");
                        ProducerProducts Pproducts = new ProducerProducts();
                        Pproducts.product = splitString[0];
                        Pproducts.count = Integer.parseInt(splitString[1]);
                        Pproducts.price = Double.parseDouble(splitString[2]);
                        
                        tempProducer.add(Pproducts);
                    }
                    P.add(tempProducer);
                }

                line = br.readLine();
                splitString = line.split("\\s+");
                ConsumersSize = Integer.parseInt(splitString[0]);
                for(int i = 0; i < Globals.ConsumersSize; i++){
                    ArrayList<ConsumerProducts> tempConsumer = new ArrayList<>();
                    line = br.readLine();
                    splitString = line.split("\\s+");
                    size = Integer.parseInt(splitString[1]);
                    for (int j = 0; j < size; j++){
                        line = br.readLine();
                        splitString = line.split("\\s+");
                        ConsumerProducts Cproducts = new ConsumerProducts();
                        Cproducts.product = splitString[0];
                        Cproducts.count = Integer.parseInt(splitString[1]);
                        
                        tempConsumer.add(Cproducts);
                    }
                    C.add(tempConsumer);
                }
            }
        }
    }
    
    /**
    * Writes data to results file
    * @param resultsFile results file name
    * @param Producers producers data array
    * @param Consumers consumers data array
    * @param ProducersSize producers array size
    * @param ConsumersSize consumers array size
    */
    static void writeTables(ArrayList< ArrayList<ProducerProducts>> P, 
            ArrayList< ArrayList<ConsumerProducts>> C) throws Exception
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(Globals.resultsFile));
        bw.close();
        try (BufferedWriter file = new BufferedWriter(new FileWriter(Globals.resultsFile, true))) {
            file.write("Producers size: " + Globals.ProducersSize + "\r\n\r\n");

            for (int i = 0; i < Globals.ProducersSize; i++)
            {
                file.write("Producer " + (i+1));
                file.newLine();
                file.write(String.format("%10s %10s %10s \r\n", "Product", "Count", "Price"));
                file.write("---------------------------------\r\n");
                for (int j = 0; j < P.get(i).size(); j++)
                {
                    file.write(j+1 + ") ");
                    file.write(String.format("%10s %10s %10s \r\n", P.get(i).get(j).product, 
                            P.get(i).get(j).count, P.get(i).get(j).price));
                }
                file.newLine();
            }
            file.newLine();
            file.write("---------------------------------\r\n");
            file.write("Consumers size: " + Globals.ProducersSize + "\r\n\r\n");

            for (int i = 0; i < Globals.ConsumersSize; i++)
            {
                file.write("Consumer " + (i+1));
                file.newLine();
                file.write(String.format("%10s %10s \r\n", "Product", "Count"));
                file.write("---------------------------------\r\n");
                for (int j = 0; j < C.get(i).size(); j++)
                {
                    file.write(j+1 + ") ");
                    file.write(String.format("%10s %10s \r\n", C.get(i).get(j).product, 
                            C.get(i).get(j).count));
                }
                file.newLine();
            }
            file.newLine();
            file.flush();
            file.close();
        }
    }
    
    /**
    * Writes data to results file
    * @param resultsFile results file name
    * @param Producers producers data array
    * @param Consumers consumers data array
    * @param ProducersSize producers array size
    * @param ConsumersSize consumers array size
    */
    static void writeResults(Buffer B) throws Exception
    {
        try (BufferedWriter file = new BufferedWriter(new FileWriter(Globals.resultsFile, true))) {
            
            ConsumerProducts[] results = B.getItems();
            file.write("Rezultatai:\r\n");
            file.write("Masyvo dydis:" + B.getSize() + "\r\n");
            file.write(String.format("%10s %10s \r\n", "Product", "Count"));
            file.write("---------------------------------\r\n");
            for(int i = 0; i < B.getSize(); i++){
                file.write(String.format("%d %10s %10s\r\n", (i+1), results[i].product, 
                        results[i].count));
            }
            file.newLine();
            file.flush();
            file.close();
        }
    }
    
}
