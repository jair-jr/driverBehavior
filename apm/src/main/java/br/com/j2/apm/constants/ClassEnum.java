package br.com.j2.apm.constants;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.j2.apm.APMException;

public enum ClassEnum {
	CURVA_ESQUERDA_AGRESSIVA("curva_esquerda_agressiva"),
	CURVA_DIREITA_AGRESSIVA("curva_direita_agressiva"),
	TROCA_FAIXA_ESQUERDA_AGRESSIVA("troca_faixa_esquerda_agressiva"),
	TROCA_FAIXA_DIREITA_AGRESSIVA("troca_faixa_direita_agressiva"),
	ACELERACAO_AGRESSIVA("aceleracao_agressiva"),
	FREADA_AGRESSIVA("freada_agressiva"),
	EVENTO_NAO_AGRESSIVO("evento_nao_agressivo");
	
	private static final Map<String, ClassEnum> CLASS_ENUMS_BY_ID = 
		Collections.unmodifiableMap(Stream.of(values()).collect(
				Collectors.toMap(
						ce -> ce.getId(), 
						Function.identity())));
	
	private String id;

	private ClassEnum(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public static ClassEnum getById(String id){
		final ClassEnum ce = CLASS_ENUMS_BY_ID.get(id);
		if(ce == null){
			throw new APMException("Class does not exist: " + id);
		}
		
		return ce;
	}
}
