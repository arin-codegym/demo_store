package com.quochuy.ai.product;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.InternalProductContext;
import com.quochuy.ai.product.dto.InternalProductFact;
import com.quochuy.ai.product.dto.ProductComparisonContext;
import com.quochuy.store.mapper.ProductMapper;
import com.quochuy.store.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductComparisonService {
	private static final int INTERNAL_PRODUCT_LIMIT = 3;
	
	private final ProductMentionExtractor mentionExtractor;
	private final ProductMapper productMapper;
	private final ExternalProductResearchService externalProductResearchService;
	
	public boolean isProductComparisonQuestion(String question) {
		return mentionExtractor.isProductComparisonQuestion(question);
	}
	
	public ProductComparisonContext resolve(String question) {
		return resolve(question, resolveInternalProduct(question));
	}
	
	public ProductComparisonContext resolve(String question, InternalProductContext internalProduct) {
		String externalQuery = mentionExtractor.buildExternalQuery(
				question,
				internalProduct.products().stream().map(InternalProductFact::name).toList()
		);
		ExternalProductContext externalProduct = externalProductResearchService.search(externalQuery);
		return new ProductComparisonContext(question, internalProduct, externalProduct);
	}
	
	public InternalProductContext resolveInternalProduct(String question) {
		List<String> candidates = mentionExtractor.extractSearchCandidates(question);
		Map<String, InternalProductFact> productsById = new LinkedHashMap<>();
		List<String> attemptedQueries = new ArrayList<>();
		
		for (String candidate : candidates) {
			if (candidate == null || candidate.isBlank()) {
				continue;
			}
			attemptedQueries.add(candidate);
			List<Product> products = productMapper.searchTopProductsForAi(
					candidate,
					INTERNAL_PRODUCT_LIMIT
			);
			if (products == null) {
				continue;
			}
			for (Product product : products) {
				InternalProductFact fact = toFact(product);
				productsById.putIfAbsent(String.valueOf(fact.productId()), fact);
				if (productsById.size() >= INTERNAL_PRODUCT_LIMIT) {
					return new InternalProductContext(
							new ArrayList<>(productsById.values()),
							attemptedQueries
					);
				}
			}
		}
		
		return new InternalProductContext(new ArrayList<>(productsById.values()), attemptedQueries);
	}
	
	public String buildInternalProgressMessage(InternalProductContext context) {
		if (context == null || !context.hasProducts()) {
			return """
					Mình chưa tìm thấy sản phẩm nội bộ khớp rõ ràng trong catalog.
					Mình sẽ tiếp tục kiểm tra dữ liệu bên ngoài và chỉ kết luận khi đủ thông tin.
					
					""";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("Mình tìm thấy sản phẩm trong shop:\n");
		for (InternalProductFact product : context.products()) {
			builder.append("- ")
					.append(product.name())
					.append(" (")
					.append(safe(product.company()))
					.append("), giá ")
					.append(formatPrice(product.price()))
					.append("\n");
		}
		builder.append("\nMình đang lấy dữ liệu sản phẩm bên ngoài để so sánh...\n\n");
		return builder.toString();
	}
	
	private InternalProductFact toFact(Product product) {
		return new InternalProductFact(
				product.getProductId(),
				product.getName(),
				product.getCompany(),
				product.getDescription(),
				product.getImage(),
				product.getPrice()
		);
	}
	
	private String formatPrice(int price) {
		return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price) + " VND";
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
}
