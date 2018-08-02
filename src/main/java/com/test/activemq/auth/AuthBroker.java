package com.test.activemq.auth;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.AbstractAuthenticationBroker;
import org.apache.activemq.security.AuthenticationBroker;
import org.apache.activemq.security.SecurityContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthBroker extends AbstractAuthenticationBroker implements AuthenticationBroker {

	private static Log log = LogFactory.getLog(AuthBroker.class);

	public AuthBroker(Broker next) {
		super(next);
	}

	@Override
	public void start() throws Exception {
		log.info("auth data init start");
		MysqlUtil.getData();
		log.info("auth data init end");
		super.start();
	}

	@Override
	public void send(ProducerBrokerExchange producerExchange, Message messageSend) throws Exception {
		log.info("send producerExchange:" + producerExchange);
		ProducerInfo info = producerExchange.getProducerState().getInfo();
		String userName = producerExchange.getConnectionContext().getUserName();
		ActiveMQDestination destination = info.getDestination();

		ActiveMQDestination destination2 = messageSend.getDestination();
		String destinationTypeAsString = destination2.getPhysicalName();
		log.info("destination is " + destinationTypeAsString + "userName is " + userName);
		// log.info("destination is "+destination+"ProducerInfo is "+info+",
		// messageSend is "+messageSend);
		if (null != destination) {
			String physicalName = destination.getPhysicalName();
			log.info("ProducerInfo.destination is " + physicalName + "userName is " + userName);
		}
		super.send(producerExchange, messageSend);
	}

	@Override
	public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {
		log.info("addProducer begin info is " + info + "context is " + context);
		ActiveMQDestination destination = info.getDestination();
		if (null != destination) {
			String physicalName = destination.getPhysicalName();
			String userName = context.getUserName();
			String physicalName2 = info.getDestination().getPhysicalName();
			log.info("ProducerInfo.destination is " + physicalName + "userName is " + userName);
			String topic = RedisUtil.get(Constants.redis.topic_auth_prex + userName);
			if (null == topic || !physicalName2.startsWith(topic)) {
				throw new SecurityException("没有权限连接该topic");
			}
		}
		super.addProducer(context, info);
	}

	@Override
	public Subscription addConsumer(ConnectionContext context, ConsumerInfo info) throws Exception {
		String physicalName = info.getDestination().getPhysicalName();
		String userName = context.getUserName();

		String topic = RedisUtil.get(Constants.redis.topic_auth_prex + userName);
		log.info("ConsumerInfo.destination is " + physicalName + "userName is " + userName + "topic is " + topic);
		if (null == topic || !physicalName.startsWith(topic)) {
			// context.getConnection().stop();
			// context.setSecurityContext(null);
			// super.stop();
			// removeConnection(context, info, new
			// SecurityException("没有权限连接该topic"));
			throw new SecurityException("没有权限连接该topic");
		}
		return super.addConsumer(context, info);
	}

	@Override
	public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {
		SecurityContext securityContext = context.getSecurityContext();
		String clientId = info.getClientId();
		RedisUtil.set(Constants.redis.login_status_prex + clientId, Constants.redis.login_status_on);
		log.info("ConnectionInfo:id is " + info.getClientId() + "ip is " + info.getClientIp());
		if (securityContext == null) {
			securityContext = authenticate(info.getUserName(), info.getPassword(), null);
			context.setSecurityContext(securityContext);
			securityContexts.add(securityContext);
		}

		try {
			super.addConnection(context, info);
		} catch (Exception e) {
			securityContexts.remove(securityContext);
			context.setSecurityContext(null);
			throw e;
		}
	}

	@Override
	public void removeConnection(ConnectionContext context, ConnectionInfo info, Throwable error) throws Exception {
		String clientId = info.getClientId();
		RedisUtil.set(Constants.redis.login_status_prex + clientId, Constants.redis.login_status_off);
		super.removeConnection(context, info, error);
	}

	public SecurityContext authenticate(String username, String password, X509Certificate[] peerCertificates)
			throws SecurityException {
		SecurityContext securityContext = null;
		String passwordC = RedisUtil.get(Constants.redis.login_auth_prex + username);
		log.info("passwordC:" + passwordC + ",password:" + password);
		if (password.equals(passwordC)) {
			// if ("admin".equals(username) && "1234".equals(password)) {
			securityContext = new SecurityContext(username) {
				@Override
				public Set<Principal> getPrincipals() {
					Set<Principal> groups = new HashSet<Principal>();
					groups.add(new GroupPrincipal("users"));
					return groups;
				}
			};
		} else {
			throw new SecurityException("验证失败");
		}
		return securityContext;
	}
}