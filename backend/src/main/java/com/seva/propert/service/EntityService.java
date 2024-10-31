package com.seva.propert.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;



public interface EntityService<T,ID> {
	@Transactional(readOnly=true)
	public List<T> findAll();
	
	@Transactional(readOnly=true)
	public Optional<T> findById(ID id) throws IllegalArgumentException;  //Exception is thrown if {id} is null
	
	@Transactional
	public T save(T entity);
	
	@Transactional
	public void delete(T entity);
}