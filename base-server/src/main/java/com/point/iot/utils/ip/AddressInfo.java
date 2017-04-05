package com.point.iot.utils.ip;

/**
 * @author Wang Zhaohui 2013-7-9 下午1:22:11 IP对应的地址信息
 */

public class AddressInfo {
	/**
	 * 国家
	 */
	private String country;
	/**
	 * 区域(华南、华北)
	 */
	private String area;
	/**
	 * 省 直辖市
	 */
	private String region;
	/**
	 * 城市
	 */
	private String city;
	/**
	 * 网络运营商
	 */
	private String isp;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public String toString() {
		return country + "," + area + "," + region + "," + city + "," + isp;
	}
}
