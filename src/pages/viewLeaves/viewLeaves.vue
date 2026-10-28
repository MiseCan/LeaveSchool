<template>
	<view class="container">
		<!-- 上方手机状态栏 通栏 -->
		<view class="status_bar">
			<view class="top_view"></view>
		</view>
		<view class="nav">
			<view class="nav-left" @click="index1">
				<image src="../../static/img/dt_store_back.png"></image>
			</view>
			<view class="nav-close" @click="index1">✕</view>
			<view class="nav-title">请假详情</view>
			<view class="nav-feedback" @click="feedback">反馈</view>
		</view>
		<view class='tab-box'>
			<view class="check-left"><view class="check-on">请假信息</view></view>
			<view class="check-right" @click="QRCode">核验二维码</view>
		</view>

		<view class="main">
			<view class="main-top-tips" v-if="isOnLeave" @click="showClearRule">
				<view class="tips-inner">
					<image src="../../static/img/wenhao.png" class="main-top-tips-image1"></image>
					<text>如何销假？</text>
				</view>
			</view>

			<view class="state-head" :style="getTopStateBackground()">
				<view class="state-head-row">
					<view class="state-head-pass">
						<image src="../../static/img/true.png"></image>
						<text>审批已通过</text>
					</view>
					<view class="state-head-person" @click="showPerson">个人信息 ></view>
				</view>
				<view class="state-head-text">{{ leave.state }}</view>
				<view class="state-head-zigzag">
					<view class="zig" v-for="n in 22" :key="n"></view>
				</view>
			</view>

			<view class="info-block">
				<view class="info-line">
					<view class="info-type">
						<text class="label">请假类型：</text><text class="value">{{ leave.type || '-' }}</text>
					</view>
					<view class="info-school">
						<text class="label">需要离校：</text><text class="value">{{ leaveSchoolText }}</text>
					</view>
				</view>
				<view class="info-line">
					<text class="label">销假规则：</text><text class="orange">离校、非离校请假需要销假</text>
					<text class="look" @click="showClearRule">查看 ></text>
				</view>
				<view class="info-line">
					<text class="label">实际休假时间：</text><text class="value">{{ actualLeaveDate }}</text>
				</view>
			</view>

			<view class="card">
				<view class="card-title">我的 请假申请</view>
				<view class="badge">{{ leave.countDate || '-' }}</view>

				<view class="row">
					<text class="row-label">开始时间：</text>
					<text class="row-value bold">{{ leave.startDate || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">结束时间：</text>
					<text class="row-value bold">{{ leave.endDate || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">审批流程：</text>
					<text class="row-value">共{{ checkSteps }}步 <text class="look" @click="showFlow">查看></text></text>
				</view>
				<view class="row">
					<text class="row-label">紧急联系人：</text>
					<text class="row-value">{{ leave.phone || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">请假原因：</text>
					<text class="row-value">{{ leave.reason || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">发起位置：</text>
					<text class="row-value blue">{{ leave.address || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">抄送人：</text>
					<text class="row-value">{{ leave.cc || '无' }}</text>
				</view>
				<view class="row">
					<text class="row-label">目的地：</text>
					<text class="row-value">{{ leave.destination || '-' }}</text>
				</view>
				<view class="row">
					<text class="row-label">宿舍信息：</text>
					<text class="row-value">{{ leave.dorm || '-' }}</text>
				</view>
				<view class="row row-file" v-if="leave.imgFile">
					<text class="row-label">附件：</text>
					<image class="file-thumb" :src="leave.imgFile" mode="aspectFill" @click="imgFileClick"></image>
				</view>
				<view class="promise">本人承诺填写的信息真实有效，并对本次提交请假申请的信息真实性负责。</view>
			</view>

			<view class="card flow-card">
				<view class="card-title flow-title">审批流程记录</view>
				<view class="flow-list">
					<view class="flow-item" v-for="(node, i) in flowNodes" :key="i" :class="{ last: i === flowNodes.length - 1 }">
						<view class="flow-dot" :class="node.color"></view>
						<view class="flow-body">
							<view class="flow-head">
								<text class="flow-name">{{ node.title }}</text>
								<text class="flow-date">{{ node.date }}</text>
							</view>
							<view class="flow-idea" v-if="node.idea">审批意见：{{ node.idea }}</view>
						</view>
					</view>
				</view>
			</view>

			<view class="bottom-space"></view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="bottom-item" @click="share">转发</view>
			<view class="bottom-item" @click="renew">申请续假</view>
			<view class="bottom-item primary" v-if="isOnLeave" @click="clear">去销假</view>
			<view class="bottom-item" v-else @click="QRCode">查看核验码</view>
		</view>

		<!-- 右侧悬浮客服：直接用参考图裁切出来的图片 -->
		<image class="service" src="../../static/img/service.png" mode="scaleToFill" @click="showService"></image>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				leave: null,
				currDate: '',
				index: 0,
				leaves: null,
			}
		},

		computed: {
			isOnLeave() {
				return this.leave && this.leave.state == '正在休假中';
			},
			leaveSchoolText() {
				if (!this.leave) {
					return '离校';
				}
				return this.leave.leaveSchool || (String(this.leave.type || '').indexOf('外出') >= 0 ? '离校' : '离校');
			},
			actualLeaveDate() {
				if (!this.leave || this.leave.state == '正在休假中') {
					return '-';
				}
				return this.leave.startDate + ' ~ ' + this.leave.endDate + '（' + this.leave.countDate + '）';
			},
			checkSteps() {
				return 1;
			},
			flowNodes() {
				if (!this.leave) {
					return [];
				}
				const nodes = [{
					title: (this.leave.name || '') + ' - 发起申请',
					date: this.leave.applyDate || '',
					color: 'blue'
				}];
				const checks = [
					{ name: this.leave.oneCheck, status: this.leave.oneCheckStatus, date: this.leave.oneCheckDate, idea: this.leave.oneCheckIdea, level: '一级' }
				];
				checks.forEach(c => {
					if (c.name) {
						nodes.push({
							title: c.level + '：' + c.name + ' - 审批通过',
							date: c.date || '',
							idea: c.idea || '',
							color: 'green'
						});
					}
				});
				if (this.leave.state == '已完成') {
					nodes.push({
						title: (this.leave.name || '') + ' - 销假成功',
						date: this.leave.clearDate || '',
						color: 'blue'
					});
				}
				return nodes;
			}
		},

		onBackPress(options) {
			if (options.from === 'navigateBack') {
				return false;
			}
			this.onreturn();
			return true;
		},

		onLoad() {
			this.leave = uni.getStorageSync("currLeaves") || {};
			this.index = uni.getStorageSync("currIndex");
			this.refreshCurrDate();
		},

		methods: {
			onreturn() {
				uni.redirectTo({
					url: '../index/index'
				});
			},

			QRCode: function() {
				uni.navigateTo({
					url: '../QRCode/QRCode',
					animationType: 'none'
				});
			},

			index1: function() {
				uni.navigateTo({
					url: '../index/index',
					animationType: 'none'
				});
			},

			refreshCurrDate: function() {
				var date = new Date();
				this.currDate = date.getFullYear() + "-" +
					(Number(date.getMonth() + 1).toString() <= 1 ? "0" + Number(date.getMonth() + 1) : Number(date.getMonth() + 1)) + "-" +
					(date.getDate().toString().length <= 1 ? "0" + date.getDate() : date.getDate()) + " " +
					(date.getHours().toString().length <= 1 ? "0" + date.getHours() : date.getHours()) + ":" +
					(date.getMinutes().toString().length <= 1 ? "0" + date.getMinutes() : date.getMinutes()) + ":" +
					(date.getSeconds().toString().length <= 1 ? "0" + date.getSeconds() : date.getSeconds());
			},

			// 个人信息
			showPerson() {
				uni.showModal({
					title: '个人信息',
					content: '姓名：' + (this.leave.name || '-') + '\n联系电话：' + (this.leave.phone || '-') + '\n宿舍：' + (this.leave.dorm || '-'),
					showCancel: false,
					confirmText: '知道了'
				});
			},

			// 销假规则说明
			showClearRule() {
				uni.showModal({
					title: '如何销假',
					content: '离校请假返校后需要在「我的 - 请假记录」中点击「去销假」，非离校请假无需销假。',
					showCancel: false,
					confirmText: '知道了'
				});
			},

			// 审批流程说明
			showFlow() {
				const names = this.flowNodes.map(n => n.title).join('\n');
				uni.showModal({
					title: '审批流程',
					content: names,
					showCancel: false,
					confirmText: '知道了'
				});
			},

			share() {
				const text = (this.leave.name || '') + ' 的请假申请：' + (this.leave.startDate || '') + ' 至 ' + (this.leave.endDate || '');
				uni.setClipboardData({
					data: text,
					success: () => this.toast('已复制，可转发给他人')
				});
			},

			renew() {
				this.toast('续假功能暂未开放');
			},

			showService() {
				this.toast('智能客服暂未开放');
			},

			feedback() {
				this.toast('感谢反馈');
			},

			toast(str) {
				uni.showToast({
					title: str,
					icon: 'none',
					duration: 1800
				});
			},

			// 去销假
			clear: function() {
				var that = this;
				uni.showModal({
					title: '提示',
					content: '确定要销假吗？',
					success: function(e) {
						if (e.confirm) {
							var date = new Date();
							that.leaves = uni.getStorageSync('todaySchoolLeaves') || [];
							that.leave.state = '已完成';
							that.leave['clearDate'] = (Number(date.getMonth() + 1).toString().length <= 1 ? "0" + Number(date.getMonth() + 1) : Number(date.getMonth() + 1)) + "-" +
								(date.getDate().toString().length <= 1 ? "0" + date.getDate() : date.getDate()) + " " +
								(date.getHours().toString().length <= 1 ? "0" + date.getHours() : date.getHours()) + ":" +
								(date.getMinutes().toString().length <= 1 ? "0" + date.getMinutes() : date.getMinutes());
							that.leaves[that.index] = that.leave;
							uni.setStorageSync('todaySchoolLeaves', that.leaves);
							uni.setStorageSync('currLeaves', that.leave);
							uni.navigateBack({
								delta: 1
							})
						}
					}
				})
			},

			// 根据请假状态设置背景渐变色
			getTopStateBackground: function() {
				if (this.leave && this.leave.state == '已完成') {
					return 'background-image: linear-gradient(#657181, #9EA8B4);';
				}
				return 'background-image: linear-gradient(#08A45A, #00DE72);';
			},

			// 单击附件图片事件
			imgFileClick: function() {
				uni.previewImage({
					urls: [this.leave.imgFile]
				})
			},
		}
	}
</script>

<style>
	page {
		background-color: #F6F7F9;
		overflow-x: hidden;
	}

	.container {
		padding: 0;
		font-size: 10.6px;
	}

	/* 状态栏通栏 */
	.status_bar {
		height: 0px;
		width: 100%;
		background-color: #545454;
	}

	.top_view {
		height: var(--status-bar-height);
		width: 100%;
		position: fixed;
		background-color: white;
		top: 0;
		z-index: 999;
	}

	/* 导航 */
	.nav {
		position: fixed;
		top: 0;
		width: 100%;
		height: 48px;
		background: white;
		border-bottom: 1px solid #EFEFEF;
		z-index: 9999;
	}

	.nav-left {
		position: absolute;
		left: 15.5px;
		top: 14px;
		width: 22px;
		height: 20px;
	}

	.nav-left image {
		width: 20px;
		height: 20px;
	}

	.nav-close {
		position: absolute;
		left: 56.5px;
		top: 11.5px;
		font-size: 24px;
		line-height: 24px;
		color: #2F2F2F;
	}

	.nav-feedback {
		position: absolute;
		right: 16px;
		top: 16px;
		font-size: 15px;
		line-height: 18px;
		color: #E64340;
	}

	.nav-title {
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		height: 48px;
		line-height: 48px;
		text-align: center;
		font-weight: 500;
		font-size: 21px;
		color: #000000;
		letter-spacing: 1px;
	}

	/* Tab */
	.tab-box {
		position: fixed;
		width: 100%;
		height: 40px;
		top: 49px;
		background-color: white;
		border-bottom: 1px solid #EFEFEF;
		z-index: 999;
		display: flex;
	}

	.check-left,
	.check-right {
		flex: 1;
		height: 40px;
		text-align: center;
		font-size: 14px;
		color: #5D6066;
		line-height: 40px;
		font-weight: 600;
	}

	.check-on {
		display: inline-block;
		box-sizing: border-box;
		height: 40px;
		line-height: 40px;
		font-weight: 600;
		color: #2D96F0;
		border-bottom: 3px solid #2D96F0;
		padding: 0 3px;
	}

	.main {
		position: relative;
		top: 88px;
		padding-bottom: 58px;
	}

	/* 顶部橙色提示条 */
	.main-top-tips {
		width: 100%;
		height: 21px;
		background-color: #FC980F;
		color: white;
		font-size: 12px;
		position: relative;
	}

	.tips-inner {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 21px;
		padding-left: 2px;
	}

	.main-top-tips-image1 {
		width: 12px;
		height: 12px;
		margin-right: 4px;
	}

	/* 顶部状态块 */
	.state-head {
		position: relative;
		height: 94px;
		overflow: hidden;
	}

	.state-head-row {
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		padding-top: 7px;
	}

	.state-head-pass {
		display: flex;
		align-items: center;
	}

	.state-head-pass image {
		width: 15px;
		height: 15px;
		margin-right: 5px;
	}

	.state-head-pass text {
		color: white;
		font-size: 17.3px;
		letter-spacing: 1px;
	}

	.state-head-person {
		position: absolute;
		right: 14px;
		top: 9px;
		color: white;
		font-size: 13px;
		opacity: .96;
	}

	.state-head-text {
		color: white;
		text-align: center;
		font-size: 31.3px;
		margin-top: 4px;
		letter-spacing: 1px;
	}

	/* 底部锯齿 */
	.state-head-zigzag {
		position: absolute;
		bottom: 0;
		left: -40px;
		width: 900px;
		height: 18px;
		overflow: hidden;
		white-space: nowrap;
		font-size: 0;
	}

	.zig {
		display: inline-block;
		margin-right: 32px;
		width: 26px;
		height: 18px;
		transform: skew(-40deg);
		background-color: #F6F7F9;
	}

	/* 类型 / 规则信息 */
	.info-block {
		background-color: white;
		padding: 4px 20px 6px;
		border-bottom: 1px solid #EFEFEF;
	}

	.info-line {
		font-size: 12px;
		line-height: 20px;
		color: #657181;
		display: flex;
		align-items: center;
	}

	.info-type {
		width: 52%;
	}

	.info-school {
		width: 48%;
	}

	.label {
		color: #98999C;
	}

	.value {
		color: #5B6067;
		font-weight: 400;
	}

	.orange {
		color: #F79A0D;
	}

	.blue {
		color: #2C8CF0;
	}

	.look {
		color: #2C8CF0;
		margin-left: 5px;
	}

	/* 卡片通用 */
	.card {
		position: relative;
		background-color: white;
		margin-top: 8px;
		padding: 6px 20px 8px;
		border-top: 1px solid #EFEFEF;
		border-bottom: 1px solid #EFEFEF;
	}

	.card-title {
		font-size: 14px;
		color: #3D3D42;
		font-weight: 500;
		line-height: 20px;
		margin-bottom: 5px;
	}

	.badge {
		position: absolute;
		right: 18px;
		top: 40px;
		color: #3C87D4;
		background: #F3F8FE;
		border: 1px solid #9CC8EF;
		border-radius: 5px;
		padding: 3px 8px;
		font-size: 14px;
		font-weight: 600;
		line-height: 20px;
	}

	.row {
		display: flex;
		align-items: flex-start;
		font-size: 12px;
		line-height: 20px;
	}

	.row-label {
		color: #98999C;
		flex-shrink: 0;
	}

	.row-value {
		color: #5B6067;
		font-weight: 400;
		margin-left: 6px;
		flex: 1;
		word-break: break-all;
	}

	/* 只有开始/结束时间是加粗的 */
	.row-value.bold {
		font-weight: 600;
	}

	/* 发起位置：蓝色链接色，常规粗细（要压过 .row-value 的默认色） */
	.row-value.blue {
		color: #5A9BD5;
		font-weight: 400;
	}

	.row-file {
		align-items: flex-start;
	}

	.file-thumb {
		width: 48px;
		height: 48px;
		margin-top: 2px;
		border-radius: 3px;
		background-color: #F2F3F5;
	}

	.promise {
		color: #F79A0D;
		font-size: 11.6px;
		line-height: 16px;
		margin-top: 8px;
	}

	/* 审批流程记录 */
	.flow-card {
		padding: 10px 20px 14px;
	}

	.flow-title {
		margin-bottom: 6px;
	}

	.flow-item {
		position: relative;
		display: flex;
		padding-bottom: 14px;
	}

	.flow-item:not(.last)::before {
		content: '';
		position: absolute;
		left: 7.6px;
		top: 16px;
		bottom: 0;
		width: 1.4px;
		background-color: #DADEE1;
	}

	.flow-dot {
		position: relative;
		z-index: 1;
		width: 14.2px;
		height: 14.2px;
		border-radius: 50%;
		border: 1.2px solid #2D96F0;
		background-color: white;
		margin-top: 1px;
		flex-shrink: 0;
	}

	.flow-dot.green {
		border-color: #00C776;
	}

	.flow-body {
		margin-left: 11px;
		flex: 1;
	}

	.flow-head {
		display: flex;
		justify-content: space-between;
		font-size: 12.5px;
		line-height: 19px;
		color: #5F656B;
		font-weight: 400;
	}

	.flow-date {
		color: #9C9FA2;
		font-weight: 400;
	}

	.flow-idea {
		margin-top: 5px;
		background-color: #F6F7F9;
		border: 1px solid #D7DDE4;
		border-radius: 4px;
		padding: 3px 7px;
		color: #A1ABB5;
		font-size: 11px;
		line-height: 16px;
	}

	/* 底部操作栏 */
	.bottom-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		width: 100%;
		height: 48px;
		background-color: white;
		display: flex;
		border-top: 1px solid #EBEBEB;
		z-index: 1000;
	}

	.bottom-item {
		flex: 1;
		text-align: center;
		line-height: 48px;
		font-size: 14.4px;
		color: #31363F;
		border-right: 1px solid #EBEBEB;
	}

	.bottom-item:last-child {
		border-right: none;
	}

	.bottom-item.primary {
		background-color: #3399FE;
		color: white;
	}

	.bottom-space {
		height: 16px;
	}

	/* 悬浮智能客服：原图裁切，86x338 → 24x95 CSS */
	.service {
		position: fixed;
		right: 0;
		bottom: 18.9%;
		width: 24px;
		height: 95px;
		z-index: 1001;
	}
</style>
