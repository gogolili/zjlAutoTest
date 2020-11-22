package base;

public class LocatorBase {
	
	private String value;
	private int timeout;
	private String locatorName;
	private ByType type;
	
	public enum ByType{
		xpath,id,linktext,name,className,cssSelector,partialLinkText,tagName
	}
	
	

	public LocatorBase(String element) {
		this.value = element;
		this.timeout = 3;
		this.type = ByType.xpath;
	}

	public LocatorBase() {
    }

	public LocatorBase(String element, int waitSec) {
		this.value = element;
		this.timeout = waitSec;
		this.type = ByType.xpath;
	}
	
	public LocatorBase(String element, int waitSec,  ByType type) {
		this.value = value;
		this.timeout = timeout;
		this.locatorName = locatorName;
		this.type = type;
	}
	
	public LocatorBase(String element, int waitSec,  ByType type ,String locatorName) {
		this.value = element;
		this.timeout = waitSec;
		this.locatorName = locatorName;
		this.type = type;
	}
	
	public String getElement() {
		return value;
	}
	
	public int getWaitSec() {
		return timeout;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getLocatorName() {
		return locatorName;
	}

	public void setLocatorName(String locatorName) {
		this.locatorName = locatorName;
	}

	public ByType getType() {
		return type;
	}

	public void setType(ByType type) {
		this.type = type;
	}
	
}
