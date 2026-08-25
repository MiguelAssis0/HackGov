package com.fiap.hackgov.clients.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.clients.internal.DTOs.ClientDTOs.*;
import com.fiap.hackgov.clients.internal.entities.*;
import com.fiap.hackgov.clients.internal.repositories.*;
import com.fiap.hackgov.shared.infra.exceptions.*;
import com.fiap.hackgov.shared.infra.security.SensitiveStringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class ClientService {
 private final ClientRepository repository;
 private final ClientServiceRecordRepository recordRepository;

 @Transactional(readOnly=true)
 public Page<Response> findAll(String query,Pageable pageable,Employee employee){
  Employee current=requireEmployee(employee);
  return repository.search(cityId(current),query==null?"":query.trim(),pageable).map(client->toResponse(client,current));
 }

 @Transactional(readOnly=true)
 public Response findById(UUID id,Employee employee){ Employee current=requireEmployee(employee); return toResponse(findScoped(id,current),current); }

 @Transactional
 public Response create(SaveRequest request,Employee employee){
  Employee current=requireEmployee(employee); String cpf=normalizeCpf(request.cpf()); validateCpf(cpf);
  String lookup=SensitiveStringConverter.lookup("client-cpf",cpf);
  if(repository.findByCityHall_IdAndCpfLookup(cityId(current),lookup).isPresent()) throw new ResourceAlreadyExistsException("Ja existe cliente com este CPF na prefeitura");
  Client client=new Client(); client.setCityHall(current.getCityHallId()); apply(client,request,cpf,lookup);
  return toResponse(repository.save(client),current);
 }

 @Transactional
 public Response update(UUID id,SaveRequest request,Employee employee){
  Employee current=requireEmployee(employee); Client client=findScoped(id,current); String cpf=normalizeCpf(request.cpf()); validateCpf(cpf);
  String lookup=SensitiveStringConverter.lookup("client-cpf",cpf);
  repository.findByCityHall_IdAndCpfLookup(cityId(current),lookup).filter(existing->!existing.getId().equals(id)).ifPresent(existing->{throw new ResourceAlreadyExistsException("Ja existe cliente com este CPF na prefeitura");});
  apply(client,request,cpf,lookup); return toResponse(repository.save(client),current);
 }

 @Transactional
 public ServiceResponse addService(UUID clientId,ServiceRequest request,Employee employee){
  Employee current=requireEmployee(employee); Client client=findScoped(clientId,current);
  ClientServiceRecord record=new ClientServiceRecord(); record.setClient(client); record.setArea(request.area().trim()); record.setDescription(request.description().trim());
  record.setObservation(request.observation()==null?"":request.observation().trim()); record.setServiceDate(request.serviceDate()==null?LocalDate.now():request.serviceDate()); record.setCreatedBy(current);
  return serviceResponse(recordRepository.save(record),canViewSensitive(current));
 }

 private void apply(Client client,SaveRequest request,String cpf,String lookup){
  client.setFullName(request.fullName().trim()); client.setNickname(clean(request.nickname())); client.setCpf(cpf); client.setCpfLookup(lookup); client.setPhone(clean(request.phone()));
  client.setSecondaryContact(clean(request.secondaryContact())); client.setAddress(clean(request.address())); client.setStateRegistration(clean(request.stateRegistration())); client.setCaf(clean(request.caf()));
 }
 private Response toResponse(Client client,Employee viewer){
  boolean sensitive=canViewSensitive(viewer); List<ServiceResponse> services=recordRepository.findByClient_IdOrderByServiceDateDescCreatedAtDesc(client.getId()).stream().map(record->serviceResponse(record,sensitive)).toList();
  return new Response(client.getId(),client.getFullName(),client.getNickname(),sensitive?formatCpf(client.getCpf()):maskCpf(client.getCpf()),
    sensitive?client.getPhone():maskPhone(client.getPhone()),sensitive?client.getSecondaryContact():maskPhone(client.getSecondaryContact()),
    sensitive?client.getAddress():maskAddress(client.getAddress()),sensitive?client.getStateRegistration():maskGeneric(client.getStateRegistration()),
    sensitive?client.getCaf():maskGeneric(client.getCaf()),!sensitive,services,client.getCreatedAt(),client.getUpdatedAt());
 }
 private ServiceResponse serviceResponse(ClientServiceRecord record,boolean sensitive){ return new ServiceResponse(record.getId(),record.getArea(),record.getDescription(),sensitive?record.getObservation():maskGeneric(record.getObservation()),record.getServiceDate(),record.getCreatedBy()==null?null:record.getCreatedBy().getFullName(),record.getCreatedAt()); }
 private Client findScoped(UUID id,Employee employee){ return repository.findByIdAndCityHall_Id(id,cityId(employee)).orElseThrow(()->new ResourceNotFoundException("Cliente nao encontrado")); }
 private Employee requireEmployee(Employee employee){ if(employee==null) throw new UnauthorizedException("E necessario estar autenticado"); cityId(employee); return employee; }
 private UUID cityId(Employee employee){ if(employee.getCityHallId()==null) throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura"); return employee.getCityHallId().getId(); }
 private boolean canViewSensitive(Employee employee){ return Roles.ADMIN.equals(employee.getRole()); }
 private String clean(String value){ return value==null?"":value.trim(); }
 private String normalizeCpf(String value){ return value==null?"":value.replaceAll("\\D",""); }
 private void validateCpf(String cpf){
  if(cpf.length()!=11||cpf.chars().distinct().count()==1) throw new BusinessException("CPF invalido");
  for(int digit=9;digit<11;digit++){int sum=0;for(int index=0;index<digit;index++)sum+=(cpf.charAt(index)-'0')*(digit+1-index);int check=(sum*10)%11;if(check==10)check=0;if(check!=cpf.charAt(digit)-'0')throw new BusinessException("CPF invalido");}
 }
 private String formatCpf(String cpf){ return cpf==null?"":cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})","$1.$2.$3-$4"); }
 private String maskCpf(String cpf){ return cpf==null||cpf.length()!=11?"***":String.format("***.%s.%s-**",cpf.substring(3,6),cpf.substring(6,9)); }
 private String maskPhone(String value){ if(value==null||value.isBlank())return "";String digits=value.replaceAll("\\D","");return digits.length()<4?"***":"(**) *****-"+digits.substring(digits.length()-4); }
 private String maskAddress(String value){ return value==null||value.isBlank()?"":"Endereco protegido"; }
 private String maskGeneric(String value){ return value==null||value.isBlank()?"":"***"; }
}
