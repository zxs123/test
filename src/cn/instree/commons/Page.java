package cn.instree.commons;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

	private static int DEFAULT_PAGE_SIZE = 10;
	private int pageSize = DEFAULT_PAGE_SIZE;
	private long start;
	private long end;
	private long totalCount;
	private long currentPageNo;
	private long totalPageCount;
	private List<T> data;
	
	public Page() {
		
		this(0, 0, DEFAULT_PAGE_SIZE, new ArrayList<T>());
	}
	
	public Page(long currentPageNo, long totalSize, int pageSize, List<T> data) {
		
		this.pageSize = pageSize;
		this.totalCount = totalSize;
		this.currentPageNo = currentPageNo;
		this.data = data;
	}
	
	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public long getCurrentPageNo() {
		return currentPageNo;
	}

	public void setCurrentPageNo(long currentPageNo) {
		this.currentPageNo = currentPageNo;
	}
	
	public void setTotalPageCount(long totalPageCount) {
		this.totalPageCount = totalPageCount;
	}

	//取总页数
	public long getTotalPageCount() {
		
		totalPageCount = totalCount / pageSize;
		if(totalCount % pageSize == 0) {
			
			return totalPageCount;
		}
		else {
			return totalPageCount + 1;
		}
		
	}
	
	 /**
     * @return the firstResult
     */
    public long getStart() {
        long temp = this.currentPageNo - 1;
        if (temp <= 0) {
            return 0;
        }
        return this.start = (this.currentPageNo - 1) * this.pageSize;
    }

    /**
     * @param firstResult
     *            the firstResult to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the lastResult
     */
    public long getEnd() {
        this.end = this.start + this.pageSize;
        return end;
    }

    /**
     * @param lastResult
     *            the lastResult to set
     */
    public void setEnd(long lastResult) {
        this.end = end;
    }
	
}
