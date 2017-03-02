package graphql.relay;


import java.util.ArrayList;
import java.util.List;

public class Connection<T> {
    private List<Edge<T>> edges = new ArrayList<>();

    private PageInfo pageInfo;

    public List<Edge<T>> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge<T>> edges) {
        this.edges = edges;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}