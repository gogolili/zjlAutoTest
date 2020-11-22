import org.testng.annotations.*;

@Test(groups = "Tomandy")
public class TestNGHelloWorld {

//    @BeforeClass
//    public void setUp(){
//        System.out.println("SetUp");
//    }
//
//    @Test
//    public void helloWorld(){
//        System.out.println("我的第一个testNg用例");
//    }
//
//    @AfterClass
//    public void tearDown(){
//        System.out.println("tearDown");
//    }

    @BeforeSuite(groups = "bfsuite")
    public void bfSuite(){
        System.out.println("TestNGHelleword BeforeSuite!");
    }

    @BeforeClass(enabled = false)
    public void bfClass(){
        System.out.println("TestNGHelloworld BeforeClass!");
    }

    @BeforeTest(dependsOnGroups = "bfSuite")  //依赖bfSuite组
    public void bfTest(){
        System.out.println("TestNGHelloWorld BeforeTest!");
    }

    @BeforeGroups(groups = {"Tom"})
    public void bfGroups(){
        System.out.println("TestNGHelloWorld BeforeGroups!");
    }
    @BeforeMethod(alwaysRun = true,dependsOnGroups = "bfSuite")//依赖bfSuite组,alwaysRun
    public void bfMethod(){
        System.out.println("TestNGHelloWorld BeforeMethod!");
    }
    @Test(groups = "Tom")
    public void helloWorldTest(){
        System.out.println("TestNGHelloWorld Test!");
    }
    @AfterSuite
    public void afSuite(){
        System.out.println("TestNGHelloWorld AfterSuite!");
    }
    @AfterClass()
    public void afClass(){
        System.out.println("TestNGHelloWorld AfterClass!");
    }

    @AfterTest
    public void afTest(){
        System.out.println("TestNGHelloWorld AfterTest!");
    }

    @AfterGroups(groups = "Tom")
    public void afGroups(){
        System.out.println("TestNGHelloWorld AfterGroups!");
    }

    @AfterMethod
    public void afMethod(){
        System.out.println("TestNGHelloWorld AfterMethod!");
    }










}
