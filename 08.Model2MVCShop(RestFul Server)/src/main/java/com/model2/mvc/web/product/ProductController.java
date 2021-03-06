package com.model2.mvc.web.product;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.review.ReviewService;

@Controller
@RequestMapping("/product/*")
public class ProductController {
	
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	
	@Autowired
	@Qualifier("reviewServiceImpl")
	private ReviewService reviewService;
	
	public ProductController() {
		System.out.println(this.getClass());
	}
	
	@Value("#{commonProperties['pageUnit']?: 3}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize']?: 2}")
	int pageSize;
	
	@RequestMapping(value="addProduct",method=RequestMethod.POST)
	public String addProduct(@ModelAttribute("product")Product product
							,HttpSession session
							) throws Exception{
		product.setManuDate(product.getManuDate().replace("-", ""));
		
		
		
		MultipartFile uploadfile = product.getUploadFile();
		if (uploadfile != null) {
			/*  if이 없어서 그런가?
			FileOutputStream fos;

			byte fileData[] = product.getUploadfile().getBytes();
			fos = new FileOutputStream(session.getServletContext().getRealPath("/") + "images\\uploadFiles\\" + product.getFileName());
			fos.write(fileData);

			fos.close();
			*/
			//이런식으로 파일명을 안받아서그런가?
			String fileName = uploadfile.getOriginalFilename();
			product.setFileName(fileName);

			File file = new File(session.getServletContext().getRealPath("/")
								+"images/uploadFiles/" + fileName);
			uploadfile.transferTo(file);
		}
		
		productService.addProduct(product);
		
		return "redirect:/product/listProduct?menu=manage";
	}
	
	@RequestMapping(value="getProduct",method=RequestMethod.GET)
	public String getProduct(@RequestParam("prodNo")int prodNo
							,HttpSession session
							,Model model
							) throws Exception{
		Product product = productService.getProduct(prodNo);
		model.addAttribute("product",product);
		
		Map<Integer,String> viewListMap = new HashMap<Integer,String>();
		if(session.getAttribute("viewListMap") != null){
			viewListMap = (HashMap<Integer,String>)session.getAttribute("viewListMap");
		}
		viewListMap.put(prodNo,product.getProdName());
		if(viewListMap.size()>6){
			viewListMap.remove(6);
		}
		System.out.println("#"+reviewService.getReviewList(prodNo));
		model.addAttribute("reviewList",reviewService.getReviewList(prodNo));
		session.setAttribute("viewListMap", viewListMap);
		
		return "forward:/product/getProduct.jsp";
	}
	@RequestMapping(value="getJsonProduct/{prodNo}",method=RequestMethod.GET)
	public void getJsonProduct(@PathVariable int prodNo
								,Model model
								) throws Exception{
		model.addAttribute("product",productService.getProduct(prodNo));
	}
	
	@RequestMapping(value="updateProductView",method=RequestMethod.GET)
	public String updateProductView(@RequestParam("prodNo")int prodNo
							,Model model
							)throws Exception{
		model.addAttribute("product",productService.getProduct(prodNo));
		return "forward:/product/updateProductView.jsp";
	}
	
	@RequestMapping(value="updateProduct",method=RequestMethod.POST)
	public String updateProduct(@ModelAttribute("product")Product product
							)throws Exception{
		productService.updateProduct(product);
		return "redirect:/product/listProduct?menu=manage";
	}
	
	@RequestMapping(value="listProduct")
	public String listProduct(@ModelAttribute("search")Search search
							,@RequestParam(value="viewSoldItem", defaultValue="off")String viewSoldItem
							,Model model)throws Exception{
		if(viewSoldItem.equals("on")){
			search.setViewSoldItem(true);
		}
		if(search.getCurrentPage()==0){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		Map<String, Object> map = productService.getProductList(search);
		Page resultPage = new Page(search.getCurrentPage()
						, ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		model.addAttribute("resultPage",resultPage);
		model.addAttribute("list",map.get("list"));
		model.addAttribute("search",search);
		return "forward:/product/listProduct.jsp";
	}
}
