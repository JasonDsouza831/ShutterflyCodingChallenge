/**
 * Created by dsoujaso on 7/18/2017.
 */

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;

public class CodingTest {
    static class LTVclass{
        String custid;
        List<AverageCustomerValue> avgcustomervalue = new ArrayList<AverageCustomerValue>();
        double totalltv;
        LTVclass(){
            custid = null;
            totalltv = 0.0;
        }
    }

    static class AverageCustomerValue{
        int weeknum;  //Week number to get range of dates which fit within this week
        int numvisitsperweek;  //Number of days in a week spent visiting sites
        int year; //year of site visit
        double expenditurepervisit;
        AverageCustomerValue(){
            //startdate=null;
            //enddate=null;
            year=0;
            numvisitsperweek=0;
            weeknum=0;
            expenditurepervisit=0.0;
        }
    }
    static 	class Pivotclass{
        String custid;
        double totalltv; //Total ltv for that customer over all site visits in all weeks
        Pivotclass(){
            custid=null;
            totalltv=0.0;
        }
    }

    /* I used Deterministic quickselect to find the proper position of element in array of LTVclass
     * Fetch the index , and match with n-x(n-x because xth from end/max needed)
      * Once found, iterate and find max element in array
      * When found n-xth smallest or xth largest and max elem, iterate and find all elems which lie between these 2 elements
      * Time - O(n)
      * space - O(n)*/
    // Note- It asks for value of x at runtime
    public static void main(String args[]) throws ParseException {
            //File e = new File("sample_input/events.txt");
            File e = new File("input/events.txt");
            List<List<String>> D = new ArrayList<List<String>>();
            Ingest(e,D);
            System.out.println("Enter the value of x");
            Scanner sc =new Scanner(System.in);
            int x =sc.nextInt();
            //int x = 6;
            //System.out.println("x value is " +x);
            TopXSimpleLTVCustomers(x, D);
    }

    public static void Ingest(File file, List<List<String>> finallist){
        try {
            File f = new File("sample_input/events.txt");
            //System.out.println(file.getPath());
            FileInputStream ft = new FileInputStream(f);
            DataInputStream in = new DataInputStream(ft);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strline;
            String []s;
            String val[];
            int strtindx=0,endindx=0;
            List<String> l =new ArrayList<String>();

            while((strline = br.readLine()) != null){

                if(strline.contains("CUSTOMER"))
                {
                    if(l.isEmpty()) {
                        l.add(strline);
                        continue;
                    }
                    else {
                        finallist.add(l);
                        l =new ArrayList<String>();
                    }
                }
                l.add(strline);
            }
            finallist.add(l);
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    /* This function does the following :-
    * Ingest all the data into List<List<String>> finallist
    *   2 Data structures used which are-
      * HashMap<String,List<AverageCustomerValue>> which stores <custid,List<weeknumber , numberofvisits, totalexpenditure for the week>>
       * Array of LTVclass (lt) which stores custid,List<weeknumber , numberofvisits, totalexpenditure that week> ,totalltv    */
    public static void TopXSimpleLTVCustomers(int x, List<List<String>> finallist) throws ParseException {
        //String []s;
        int ltvarlen;
        String custidpair = null, amountpair = null,datepair=null;
        int strtindx=0,endindx=0,it=0;
        boolean flag;
        LTVclass []lt =new LTVclass[finallist.size()];
        LTVclass lc;
        HashMap<String,List<AverageCustomerValue>> h =new HashMap<String,List<AverageCustomerValue>>();
        int year1=0,week1=0;
        ltvarlen=0;
        for(List<String> l : finallist) {
            lc= new LTVclass();
            List<AverageCustomerValue> l12 = new ArrayList<AverageCustomerValue>();
            flag=false;
            ltvarlen++;
            for (String s : l) {
                if (s.contains("customer_id") || s.contains("SITE_VISIT") || s.contains("total_amount")) {

                    if (s.contains("total_amount")) { //Fetch the amount, update the entry for the customer for that week if it falls in same week
                        strtindx = s.indexOf("total_amount");
                        endindx = s.indexOf("USD", strtindx);
                        amountpair = s.substring(strtindx, endindx);
                        amountpair = amountpair.replaceAll("[^a-zA-Z0-9:.]+", "");
                        amountpair = amountpair.split(":")[1];
                        if(h.get(lc.custid)!=null) {
                            //ltvarlen--;
                            l12 = h.get(lc.custid);
                            for (int i = 0; i < l12.size(); i++) {
                                if (l12.get(i).weeknum == week1 && l12.get(i).year == year1) {
                                    l12.get(i).expenditurepervisit += 52*(Double.parseDouble(amountpair))*10; //LTV is calculated here
                                    lc.totalltv+=l12.get(i).expenditurepervisit;
                                    //lc.totalltv+=l12.get(i).expenditurepervisit;
                                    h.put(lc.custid, l12);
                                }
                                else
                                {
                                    lc.totalltv+=l12.get(i).expenditurepervisit;
                                }
                            }
                            //lc.totalltv += Double.parseDouble(amountpair);
                            if(flag == true) //Flag is used to update existing entry into array of LTVclass rather than searching that customer from beginning
                            {
                                for(int i= 0;i<it;i++)
                                {
                                    if(lt[i].custid.equals(lc.custid)) {
                                        lt[i].totalltv = lc.totalltv;
                                        lt[i].avgcustomervalue = l12;
                                    }
                                }
                            }
                            else
                                lt[it++]=lc;
                        }
                    }
                    if (s.contains("customer_id")) {
                        strtindx = s.indexOf("customer_id");
                        endindx = s.indexOf(",", strtindx);
                        custidpair = s.substring(strtindx, endindx);
                        custidpair = custidpair.replaceAll("[^a-zA-Z0-9:]+", "");
                        custidpair = custidpair.split(":")[1];
                        lc.custid = custidpair;

                    }
                    if (s.contains("SITE_VISIT")) { //If user has visited the site, fetch date, and update number of visits or set number of visits if user never visited site
                        strtindx = s.indexOf("event_time");
                        endindx = s.indexOf("T", strtindx);
                        datepair = s.substring(strtindx, endindx);
                        datepair = datepair.replaceAll("[^a-zA-Z0-9:-]+", "");
                        datepair = datepair.split(":")[1];
                        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
                        Date d=dateFormat.parse(datepair);
                        Calendar c=Calendar.getInstance();
                        c.setTime(d);
                        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                        year1 =c.get(c.YEAR);
                        week1=c.get(c.WEEK_OF_YEAR);

                        //List<AverageCustomerValue> l12;
                        if(h.get(lc.custid)!=null) { //If customer already exists, dont need to add new entry
                            ltvarlen--;
                            flag=true;
                            l12 = h.get(lc.custid);
                            int i=0;
                            for (i = 0; i < l12.size(); i++) {
                                if (l12.get(i).weeknum == week1 && l12.get(i).year == year1) { //If dates fall in same week,update numbervisits
                                    l12.get(i).numvisitsperweek += 1;
                                    h.put(lc.custid, l12);
                                    break;
                                }
                            }
                            if(i==l12.size()) //meaning Customer exists but site visits dont belong to existing week
                            {
                                AverageCustomerValue av = new AverageCustomerValue();
                                av.weeknum= week1;
                                av.year=year1;
                                av.numvisitsperweek=1;
                                h.get(lc.custid).add(av);
                            }
                        }
                        else
                        {
                            AverageCustomerValue av = new AverageCustomerValue();
                            av.weeknum= week1;
                            av.year=year1;
                            av.numvisitsperweek=1;
                            h.put(lc.custid,new ArrayList<AverageCustomerValue>());
                            h.get(lc.custid).add(av);
                            lc.avgcustomervalue.add(av);
                            l12.add(av);
                        }
                    }
                }
                //h.put(custidpair, lc);
            }
            //lt[it++]=lc;
        }

        //System.out.println("ltvarlen - x is " + (ltvarlen - x));
        System.out.println("Following are the ltv entries for customers:-");
        System.out.println("Customer id     Ltv for that customer");
        for(int i=0;i<ltvarlen;i++)
        {
            System.out.println(lt[i].custid +"\t\t" +lt[i].totalltv);
        }
        System.out.println("\n\n");
        LTVclass ind = TopXSimpleLTVUtils(ltvarlen - x,finallist,0,ltvarlen-1,lt,ltvarlen);
        //int index = Partition(x,finallist,0,ltvarlen-1,lt,ltvarlen);
        if(ind.totalltv==-1) {
            System.out.println("k is greater than customers in array");
            return;
        }
        else
            System.out.println("xth elem is " +ind.totalltv);
        double max = Double.MIN_VALUE;
        for(int i=0;i<ltvarlen;i++)
        {
            if(lt[i].totalltv>max)
                max=lt[i].totalltv;
        }
        System.out.println("Max element is "+max);
        System.out.println("Top "+x+" Customers are :-");
        for(int i=0;i<ltvarlen;i++)
        {
            if(lt[i].totalltv>ind.totalltv && lt[i].totalltv<=max)
            {
                    System.out.println(lt[i].custid +"\t" +lt[i].totalltv);
            }
        }

    }

    public static LTVclass TopXSimpleLTVUtils(int x, List<List<String>> finallist,int l,int r,LTVclass []lt ,int ltvarlen)
    {

        if(l == r) return lt[l];
        /*if(x>=r-l+1) {
            LTVclass lx = new LTVclass();
            lx.totalltv=-1;
            return lx;
        } */
        int index = Partition(x,finallist,l,r,lt,ltvarlen);
        //System.out.println("index of pivot is" +index);
        int len =index-l+1;
        if( len == x)
            return lt[index];
        //System.out.println("len is " +len + " x is " +x);
        if(len > x){
            ltvarlen = index-l+1;
            return TopXSimpleLTVUtils(x,finallist,l,index-1,lt,ltvarlen);
        }
        else {
            ltvarlen =r-index; //r -index
            return TopXSimpleLTVUtils(x - len, finallist, index + 1, r, lt, ltvarlen);
        }

    }
    public static int Partition(int x, List<List<String>> finallist,int l,int r, LTVclass []ltvar, int ltvarlen){
        Pivotclass pivot = new Pivotclass();
        //int pivot=0;
        /*System.out.println("Before swap");
        for(int i =l;i<=r;i++)
            System.out.print(ltvar[i].totalltv +"\t"); */

        pivot = PivotFind(finallist,l,r,ltvar,ltvarlen);
        //i =l and j = r
        int p =l,q=r;
        while(l<r)
        {
            while(ltvar[l].totalltv<pivot.totalltv)
             l++;
            while(ltvar[r].totalltv>pivot.totalltv)
                r--;
            if(ltvar[l].totalltv==ltvar[r].totalltv)
                l++;
            else
            {
                LTVclass tmp=ltvar[l];
                ltvar[l]=ltvar[r];
                ltvar[r]=tmp;
            }
        }
        //System.out.println(pivot.totalltv);
        /*System.out.println("Pivot elem is " +ltvar[r].totalltv);
        System.out.println("After swap");
        for(int i =p;i<=q;i++)
            System.out.print(ltvar[i].totalltv +"\t"); */
        return r;
    }

    public static Pivotclass PivotFind(List<List<String>> finallist,int l,int r, LTVclass []ltvar, int ltvarlen){
        int n =(r-l+1);
        LTVclass []t;
        int medianindex=0,count=0;

        LTVclass median[] =new LTVclass[(int) Math.ceil((double)n/5)];
        //System.out.println("\n" +ltvarlen);
        Pivotclass pt[] = new Pivotclass[n];
        int u=l,v=r;
        for(int i =0;i<n && u<=v;i++)
        {
            pt[i] = new Pivotclass();
            pt[i].custid =ltvar[u].custid;
            //ltvar[i].avgcustomervalue = null;
            pt[i].totalltv = ltvar[u].totalltv;
            u++;
        }

        if(n<=9)
        {
            Collections.sort(Arrays.asList(pt),new Comparator<Pivotclass> (){
                public int compare(Pivotclass l1,Pivotclass l2){
                    return (int) (l1.totalltv - l2.totalltv);
                }
            });
            return pt[pt.length/2];
        }

        while(l<=r)
        {
            t = new LTVclass[Math.min(5,r-l+1)];
            for(int i=0;i<t.length && l<=r; i++)
            {
                t[i]= new LTVclass();
                t[i].custid =ltvar[l].custid;
                t[i].totalltv=ltvar[l].totalltv;
                l++;
                //System.out.println(entry.getKey());
            }
            Collections.sort(Arrays.asList(t),new Comparator<LTVclass> (){
                    public int compare(LTVclass l1,LTVclass l2){
                        return (int) (l1.totalltv - l2.totalltv);
                    }
            });
            median[medianindex] =t[t.length/2];
            medianindex++;
            //count=0;
        }

        return PivotFind(finallist,0,median.length-1, median,median.length);
    }
}

